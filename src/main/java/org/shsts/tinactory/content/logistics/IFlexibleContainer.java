package org.shsts.tinactory.content.logistics;

import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.gui.Layout;

public interface IFlexibleContainer extends IContainer {
    void setLayout(Layout layout);

    void resetLayout();
}
