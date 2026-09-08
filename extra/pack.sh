#!/bin/bash

if [ -z "$base_dir" ]; then
  base_dir="$(pwd)/scratch"
fi
mod_dir="$(dirname $base_dir)"

if [ -f "$base_dir/env.sh" ]; then
  source $base_dir/env.sh
fi

get_property() {
  local var=${2:-$1}
  local ret=$(grep "$1=" $mod_dir/gradle.properties | sed "s/$1=//g")
  echo "$var=$ret"
  export $var=$ret
}

get_property minecraft_version mc_version
get_property mod_version version
get_property tinycorelib_version
get_property extra_resources_version resource_version

out_dir="$(mktemp -d)"
log_dir="$base_dir/pack"
client_zip="$base_dir/tinactory-modpack-$version.zip"
server_zip="$base_dir/tinactory-server-$version.zip"
client_dir=$out_dir/client
override_dir=$client_dir/overrides
server_dir=$out_dir/server
mods_dir=$out_dir/mods

trap 'rm -fr -- "$out_dir"' EXIT
mkdir -p $log_dir
rm -fr $log_dir/*.log

echo "================================================="
echo "Build mod"
mod_jar="$mod_dir/mod/build/libs/tinactory-$mc_version-$version.jar"
pushd $mod_dir >/dev/null
./gradlew clean >$log_dir/clean.log 2>&1 && \
  rm -fr $mod_dir/mod/src/generated/* && \
  ./gradlew runData >$log_dir/runData.log 2>&1 && \
  ./gradlew build >$log_dir/build.log 2>&1
stat=$?
popd >/dev/null

if (( $stat != 0 )); then
  echo "Build failed: $stat"
  exit 1
fi

if [ ! -f $mod_jar ]; then
  echo "Mod jar not found"
  exit 1
fi

echo "Resolve dependencies"
mkdir -p $client_dir
mkdir -p $server_dir
mkdir -p $mods_dir

if [ -d $base_dir/venv ]; then
  python=$base_dir/venv/bin/python
else
  python="python"
fi

$python $mod_dir/extra/pack.py \
  --lock_file $mod_dir/extra/dependencies.lock.json \
  --pack_version $version \
  --server_output $server_dir \
  --mod_output $mods_dir \
  >$client_dir/manifest.json \
  2>$log_dir/dependencies.log
stat=$?

if (( $stat != 0 )); then
  echo "Resolve dependencies failed: $stat"
  exit 1
fi

echo "Install neoforge"
pushd $server_dir >/dev/null
java -jar neoforge-*-installer.jar --installServer >$log_dir/install.log 2>&1
rm neoforge-*-installer.jar neoforge-*-installer.jar.log
popd >/dev/null

echo "Download tinycorelib"
tinycorelib_fullversion="$mc_version-$tinycorelib_version"
tinycorelib_out="$override_dir/mods/tinycorelib-$tinycorelib_fullversion.jar"
mkdir -p $override_dir/mods
wget -O $tinycorelib_out \
  https://www.shsts.org/m2/org/shsts/tinycorelib/core/$tinycorelib_fullversion/core-$tinycorelib_fullversion.jar \
  2>$log_dir/tinycorelib.log
stat=$?

if (( $stat != 0 )); then
  echo "Download tinycorelib failed: $stat"
  exit 1
fi

echo "Copy mods"
mkdir -p $server_dir/mods
cp $mod_jar $override_dir/mods/
cp $mod_jar $server_dir/mods/
mv $mods_dir/* $server_dir/mods/
cp $tinycorelib_out $server_dir/mods/

echo "Copy extra resource"
resource_zip="$mod_dir/libs/tinactory_extra_resources_$resource_version.zip"
if [ ! -f $resource_zip ]; then
  echo "Extra resources zip not found"
  exit 1
fi
mkdir -p $override_dir/resourcepacks
cp $resource_zip $override_dir/resourcepacks/

echo "Copy extra configs"
mkdir -p $override_dir/config
mkdir -p $server_dir/config

echo "FTB"
cp -r $mod_dir/extra/ftbquests $override_dir/config/
cp -r $mod_dir/extra/ftbquests $server_dir/config/
cp $mod_dir/extra/ftbteambases-server.snbt $override_dir/config/
cp $mod_dir/extra/ftbteambases-server.snbt $server_dir/config/

echo "JEI"
cp -r $mod_dir/extra/jei $override_dir/config/

echo "JEC"
cp $mod_dir/extra/jecharacters-extra.json $override_dir/config/

echo "Default Options"
cat >$override_dir/config/defaultoptions-common.toml <<EOF
defaultDifficulty = "PEACEFUL"
defaultResourcePacks = ["vanilla", "file/tinactory_extra_resources_$resource_version.zip", "mod_resources"]
lockDifficulty = false
EOF

echo "World Generation"
cp -r $mod_dir/extra/defaultworldtype $override_dir/config/
cp $mod_dir/extra/server.properties $server_dir/
cat >$server_dir/config/tinactory-server.toml <<EOF
[technology]
team_provider = "FTB_TEAMS"
ftb_unaffiliated_player_policy = "NO_TEAM"
EOF

echo "Pack zip"
pushd $client_dir >/dev/null
rm -f $client_zip
zip -r $client_zip . >$log_dir/client_zip.log 2>&1
popd >/dev/null

pushd $server_dir >/dev/null
rm -f $server_zip
zip -r $server_zip . >$log_dir/server_zip.log 2>&1
popd >/dev/null

echo "================================================="
echo "Finish!"
echo "Saved client to $client_zip"
echo "Saved server to $server_zip"
