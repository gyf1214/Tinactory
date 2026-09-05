#!/usr/bin/env python3

import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime

import requests


# ---------------------------------------------------------------------------
# Pack configuration
# ---------------------------------------------------------------------------

PACK_NAME = "Tinactory"
PACK_AUTHOR = "Tiny"

MINECRAFT_VERSION = "1.21.1"
NEOFORGE_VERSION_PREFIX = "21.1"

CLIENT_CURSEFORGE_PROJECT_IDS = [
    # Core
    854949,     # Fusion
    238222,     # JEI
    324717,     # Jade
    289412,     # FTB Quests
    889915,     # FTB XMod Compat
    943925,     # FTB Filter Systems
    314906,     # FTB Chunks
    1197857,    # FTB Team Bases
    232131,     # Default Options
    373014,     # Default World Type

    # Quality of Life
    230976,     # Fast Leaf Decay
    250702,     # JEI Chinese Search
]

SERVER_CURSEFORGE_PROJECT_IDS = [
    # Core
    324717,     # Jade
    289412,     # FTB Quests
    889915,     # FTB XMod Compat
    943925,     # FTB Filter Systems
    314906,     # FTB Chunks
    1197857,    # FTB Team Bases

    # Quality of Life
    230976,     # Fast Leaf Decay
]

# Projects for which beta files are allowed. This is shared by client/server
# because it controls version resolution rather than target membership.
BETA_PROJECT_IDS = {
    238222,     # JEI
    373014,     # Default World Type
}

CURSEFORGE_API_BASE = "https://api.curseforge.com/v1"
CURSEFORGE_NEOFORGE_LOADER_TYPE = 6
CURSEFORGE_RELEASE_TYPE = 1
CURSEFORGE_REQUIRED_DEPENDENCY = 3

NEOFORGE_MAVEN_BASE = (
    "https://maven.neoforged.net/releases/"
    "net/neoforged/neoforge"
)
NEOFORGE_METADATA_URL = NEOFORGE_MAVEN_BASE + "/maven-metadata.xml"

REQUEST_TIMEOUT = 30
DOWNLOAD_CHUNK_SIZE = 1024 * 1024


class BuildError(RuntimeError):
    pass


def log(message):
    print(message, file=sys.stderr)


def version_key(version):
    if not re.fullmatch(r"\d+(?:\.\d+)+", version):
        raise ValueError("Unsupported version: {!r}".format(version))
    return tuple(int(part) for part in version.split("."))


def download_to_file(session, url, output_path, headers=None):
    """Download URL to output_path, replacing it only after success."""
    partial_path = output_path + ".part"

    try:
        with session.get(
            url,
            headers=headers,
            stream=True,
            timeout=REQUEST_TIMEOUT,
        ) as response:
            response.raise_for_status()
            with open(partial_path, "wb") as output:
                for chunk in response.iter_content(DOWNLOAD_CHUNK_SIZE):
                    if chunk:
                        output.write(chunk)
        os.replace(partial_path, output_path)
    except (requests.RequestException, OSError) as exc:
        try:
            os.remove(partial_path)
        except FileNotFoundError:
            pass
        raise BuildError(
            "Failed to download {}: {}".format(url, exc)
        ) from exc


def load_dependency_lock(path):
    if not os.path.exists(path):
        log("Dependency lock file does not exist: {}".format(path))
        return {"neoforge": None, "mods": {}}

    log("Loading dependency lock file: {}".format(path))
    try:
        with open(path, "r", encoding="utf-8") as input_file:
            data = json.load(input_file)
    except (OSError, ValueError) as exc:
        raise BuildError(
            "Failed to load dependency lock file {}: {}".format(path, exc)
        ) from exc

    if not isinstance(data, dict):
        raise BuildError(
            "Dependency lock file {} must contain a JSON object".format(path)
        )

    neoforge_version = data.get("neoforge")
    if neoforge_version is not None and not isinstance(neoforge_version, str):
        raise BuildError(
            "Invalid NeoForge version in dependency lock file {}".format(path)
        )

    raw_mods = data.get("mods", {})
    if not isinstance(raw_mods, dict):
        raise BuildError(
            "Invalid mods object in dependency lock file {}".format(path)
        )

    mods = {}
    for project_id_text, file_id in raw_mods.items():
        try:
            project_id = int(project_id_text)
        except (TypeError, ValueError) as exc:
            raise BuildError(
                "Invalid CurseForge project ID in dependency lock file {}: "
                "{!r}".format(path, project_id_text)
            ) from exc

        if project_id <= 0 or not isinstance(file_id, int) or file_id <= 0:
            raise BuildError(
                "Invalid CurseForge lock entry in {}: {!r}: {!r}".format(
                    path, project_id_text, file_id
                )
            )
        mods[project_id] = file_id

    return {
        "neoforge": neoforge_version,
        "mods": mods,
    }


def save_dependency_lock(path, neoforge_version, resolved_files):
    data = {
        "neoforge": neoforge_version,
        "mods": {
            str(project_id): file_info["id"]
            for project_id, file_info in sorted(resolved_files.items())
        },
    }

    directory = os.path.dirname(os.path.abspath(path))
    try:
        os.makedirs(directory, exist_ok=True)
        temporary_path = path + ".part"
        with open(temporary_path, "w", encoding="utf-8") as output_file:
            json.dump(data, output_file, indent=2, sort_keys=True)
            output_file.write("\n")
        os.replace(temporary_path, path)
    except OSError as exc:
        try:
            os.remove(path + ".part")
        except FileNotFoundError:
            pass
        raise BuildError(
            "Failed to save dependency lock file {}: {}".format(path, exc)
        ) from exc

    log("Saved dependency lock file: {}".format(path))


def get_neoforge_version(session, locked_version):
    if locked_version is not None:
        prefix = NEOFORGE_VERSION_PREFIX + "."
        if not locked_version.startswith(prefix):
            raise BuildError(
                "Locked NeoForge version {} does not match configured prefix {}"
                .format(locked_version, NEOFORGE_VERSION_PREFIX)
            )
        log("Using locked NeoForge version {}".format(locked_version))
        return locked_version

    return get_latest_neoforge_version(session)


def get_latest_neoforge_version(session):
    log(
        "Resolving latest NeoForge {}.x release...".format(
            NEOFORGE_VERSION_PREFIX
        )
    )

    try:
        response = session.get(
            NEOFORGE_METADATA_URL,
            timeout=REQUEST_TIMEOUT,
        )
        response.raise_for_status()
        root = ET.fromstring(response.content)
    except requests.RequestException as exc:
        raise BuildError(
            "Failed to download NeoForge metadata: {}".format(exc)
        ) from exc
    except ET.ParseError as exc:
        raise BuildError("NeoForge Maven metadata is invalid XML") from exc

    prefix = NEOFORGE_VERSION_PREFIX + "."
    versions = [
        element.text.strip()
        for element in root.findall("./versioning/versions/version")
        if element.text
        and element.text.startswith(prefix)
        and re.fullmatch(r"\d+(?:\.\d+)+", element.text.strip())
    ]

    if not versions:
        raise BuildError(
            "No stable NeoForge release found for prefix {}".format(
                NEOFORGE_VERSION_PREFIX
            )
        )

    latest = max(versions, key=version_key)
    log("Resolved NeoForge {}".format(latest))
    return latest


def download_neoforge_installer(session, version, output_dir):
    filename = "neoforge-{}-installer.jar".format(version)
    url = "{}/{}/{}".format(NEOFORGE_MAVEN_BASE, version, filename)
    output_path = os.path.join(output_dir, filename)

    log("Downloading NeoForge installer: {}".format(filename))
    download_to_file(session, url, output_path)
    log("Downloaded NeoForge installer to {}".format(output_path))


def parse_file_date(value):
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def get_latest_mod_release(session, project_id):
    try:
        response = session.get(
            "{}/mods/{}/files".format(CURSEFORGE_API_BASE, project_id),
            params={
                "gameVersion": MINECRAFT_VERSION,
                "modLoaderType": CURSEFORGE_NEOFORGE_LOADER_TYPE,
                "pageSize": 50,
            },
            timeout=REQUEST_TIMEOUT,
        )
        response.raise_for_status()
        files = response.json()["data"]
    except requests.RequestException as exc:
        raise BuildError(
            "Failed to query CurseForge project {}: {}".format(
                project_id, exc
            )
        ) from exc
    except (KeyError, TypeError, ValueError) as exc:
        raise BuildError(
            "CurseForge returned invalid data for project {}".format(
                project_id
            )
        ) from exc

    releases = [
        file_info
        for file_info in files
        if (
            project_id in BETA_PROJECT_IDS
            or file_info.get("releaseType") == CURSEFORGE_RELEASE_TYPE
        )
        and file_info.get("isAvailable", True)
        and isinstance(file_info.get("id"), int)
        and isinstance(file_info.get("fileDate"), str)
        and isinstance(file_info.get("fileName"), str)
    ]

    if not releases:
        raise BuildError(
            "No stable NeoForge release found for project {} on Minecraft {}"
            .format(project_id, MINECRAFT_VERSION)
        )

    return max(
        releases,
        key=lambda file_info: parse_file_date(file_info["fileDate"]),
    )


def get_mod_file(session, project_id, file_id):
    try:
        response = session.get(
            "{}/mods/{}/files/{}".format(
                CURSEFORGE_API_BASE,
                project_id,
                file_id,
            ),
            timeout=REQUEST_TIMEOUT,
        )
        response.raise_for_status()
        file_info = response.json()["data"]
    except requests.RequestException as exc:
        raise BuildError(
            "Failed to query locked CurseForge project {}, file {}: {}".format(
                project_id, file_id, exc
            )
        ) from exc
    except (KeyError, TypeError, ValueError) as exc:
        raise BuildError(
            "CurseForge returned invalid data for locked project {}, file {}"
            .format(project_id, file_id)
        ) from exc

    if (
        not isinstance(file_info, dict)
        or file_info.get("id") != file_id
        or not isinstance(file_info.get("fileName"), str)
    ):
        raise BuildError(
            "CurseForge returned invalid metadata for locked project {}, "
            "file {}".format(project_id, file_id)
        )

    return file_info


def get_mod_download_url(session, project_id, file_id):
    try:
        response = session.get(
            "{}/mods/{}/files/{}/download-url".format(
                CURSEFORGE_API_BASE,
                project_id,
                file_id,
            ),
            timeout=REQUEST_TIMEOUT,
        )
        response.raise_for_status()
        url = response.json()["data"]
    except requests.RequestException as exc:
        raise BuildError(
            "Failed to get download URL for CurseForge project {}, "
            "file {}: {}".format(project_id, file_id, exc)
        ) from exc
    except (KeyError, TypeError, ValueError) as exc:
        raise BuildError(
            "CurseForge returned an invalid download URL response for "
            "project {}, file {}".format(project_id, file_id)
        ) from exc

    if not isinstance(url, str) or not url:
        raise BuildError(
            "CurseForge did not provide a download URL for project {}, "
            "file {}".format(project_id, file_id)
        )

    return url


def download_mod(session, project_id, file_info, output_dir):
    file_id = file_info["id"]
    file_name = file_info["fileName"]
    output_path = os.path.join(output_dir, file_name)

    url = get_mod_download_url(session, project_id, file_id)
    log(
        "Downloading project {}: {} (file ID {})".format(
            project_id, file_name, file_id
        )
    )

    # The returned download URL does not need the CurseForge API key.
    download_to_file(
        session,
        url,
        output_path,
        headers={"Accept": "*/*", "x-api-key": None},
    )
    log("Downloaded {}".format(output_path))


class ModResolver:
    """Resolve CurseForge projects once and reuse the exact result."""

    def __init__(self, session, locked_file_ids):
        self.session = session
        self.locked_file_ids = locked_file_ids
        self.resolved_files = {}

    def get_file(self, project_id, requested_by=None):
        if not isinstance(project_id, int) or project_id <= 0:
            raise BuildError(
                "Invalid CurseForge project ID: {!r}".format(project_id)
            )

        if project_id in self.resolved_files:
            return self.resolved_files[project_id]

        if requested_by is None:
            log("Resolving CurseForge project {}...".format(project_id))
        else:
            log(
                "Resolving dependency {}, required by project {}...".format(
                    project_id, requested_by
                )
            )

        locked_file_id = self.locked_file_ids.get(project_id)
        if locked_file_id is not None:
            log(
                "Using locked CurseForge project {} file ID {}".format(
                    project_id, locked_file_id
                )
            )
            file_info = get_mod_file(
                self.session, project_id, locked_file_id
            )
        else:
            file_info = get_latest_mod_release(self.session, project_id)

        self.resolved_files[project_id] = file_info

        log(
            "Resolved project {}: {} (file ID {})".format(
                project_id,
                file_info["fileName"],
                file_info["id"],
            )
        )
        return file_info

    def resolve_tree(self, project_ids, target_name):
        """Resolve roots and required dependencies for one target."""
        result = []
        seen_project_ids = set()
        resolving_project_ids = set()

        def resolve_project(project_id, requested_by=None):
            if not isinstance(project_id, int) or project_id <= 0:
                raise BuildError(
                    "Invalid CurseForge project ID: {!r}".format(project_id)
                )

            if project_id in seen_project_ids:
                if requested_by is None:
                    log(
                        "Skipping duplicate {} top-level project {}".format(
                            target_name, project_id
                        )
                    )
                return

            # Protect against circular dependency relationships within this
            # target while still allowing shared cached resolutions globally.
            if project_id in resolving_project_ids:
                return
            resolving_project_ids.add(project_id)

            try:
                latest = self.get_file(project_id, requested_by=requested_by)
                dependencies = latest.get("dependencies", [])

                if not isinstance(dependencies, list):
                    raise BuildError(
                        "Invalid dependency list for CurseForge project {}, "
                        "file {}".format(project_id, latest["id"])
                    )

                # Preserve the original scripts' ordering: the project itself
                # appears before its recursively resolved dependencies.
                seen_project_ids.add(project_id)
                result.append((project_id, latest))

                for dependency in dependencies:
                    if not isinstance(dependency, dict):
                        continue
                    if (
                        dependency.get("relationType")
                        != CURSEFORGE_REQUIRED_DEPENDENCY
                    ):
                        continue

                    dependency_project_id = dependency.get("modId")
                    if not isinstance(dependency_project_id, int):
                        raise BuildError(
                            "Invalid required dependency in CurseForge project "
                            "{}, file {}: {!r}".format(
                                project_id, latest["id"], dependency
                            )
                        )

                    resolve_project(
                        dependency_project_id,
                        requested_by=project_id,
                    )
            finally:
                resolving_project_ids.discard(project_id)

        for project_id in project_ids:
            resolve_project(project_id)

        return result


def create_manifest(pack_version, neoforge_version, resolved_mods):
    files = [
        {
            "projectID": project_id,
            "fileID": file_info["id"],
            "required": True,
        }
        for project_id, file_info in resolved_mods
    ]

    return {
        "minecraft": {
            "version": MINECRAFT_VERSION,
            "modLoaders": [
                {
                    "id": "neoforge-{}".format(neoforge_version),
                    "primary": True,
                }
            ],
        },
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": PACK_NAME,
        "version": pack_version,
        "author": PACK_AUTHOR,
        "files": files,
        "overrides": "overrides",
    }


def parse_arguments():
    parser = argparse.ArgumentParser(
        description=(
            "Generate a client CurseForge manifest and download the matching "
            "NeoForge server installer and server mod JARs"
        )
    )
    parser.add_argument(
        "--pack_version",
        required=True,
        help="Version written to the client manifest.json",
    )
    parser.add_argument(
        "--server_output",
        required=True,
        help="Directory in which to download the NeoForge installer",
    )
    parser.add_argument(
        "--mod_output",
        required=True,
        help="Directory in which to download resolved server mod JARs",
    )
    parser.add_argument(
        "--lock_file",
        required=True,
        help="Dependency lock JSON to read and update",
    )
    return parser.parse_args()


def main():
    args = parse_arguments()

    api_key = os.environ.get("CURSEFORGE_API_KEY")
    if not api_key:
        print("Error: CURSEFORGE_API_KEY is not set", file=sys.stderr)
        return 1

    try:
        os.makedirs(args.server_output, exist_ok=True)
        os.makedirs(args.mod_output, exist_ok=True)
    except OSError as exc:
        print(
            "Error: failed to create output directories: {}".format(exc),
            file=sys.stderr,
        )
        return 1

    session = requests.Session()
    session.headers.update(
        {
            "Accept": "application/json",
            "x-api-key": api_key,
        }
    )

    try:
        dependency_lock = load_dependency_lock(args.lock_file)

        # Resolve NeoForge exactly once, preferring the locked version. This
        # one version is used by both the client manifest and server installer.
        neoforge_version = get_neoforge_version(
            session, dependency_lock["neoforge"]
        )

        # One resolver/cache is shared by both target trees. Locked file IDs
        # are reused when present; missing projects are resolved normally.
        resolver = ModResolver(session, dependency_lock["mods"])
        client_mods = resolver.resolve_tree(
            CLIENT_CURSEFORGE_PROJECT_IDS,
            "client",
        )
        server_mods = resolver.resolve_tree(
            SERVER_CURSEFORGE_PROJECT_IDS,
            "server",
        )

        # Persist the complete resolved union only after both dependency trees
        # have been resolved successfully.
        save_dependency_lock(
            args.lock_file,
            neoforge_version,
            resolver.resolved_files,
        )

        download_neoforge_installer(
            session,
            neoforge_version,
            args.server_output,
        )

        for project_id, file_info in server_mods:
            download_mod(
                session,
                project_id,
                file_info,
                args.mod_output,
            )

        manifest = create_manifest(
            args.pack_version,
            neoforge_version,
            client_mods,
        )
    except BuildError as exc:
        print("Error: {}".format(exc), file=sys.stderr)
        return 1
    finally:
        session.close()

    json.dump(manifest, sys.stdout, indent=2)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
