package inventory;

import shared.model.User;
import inventory.ui.InventoryPanel;

/** Compatibility entry for the original inventory module path. */
@SuppressWarnings("serial")
public class inventory extends InventoryPanel {
    /** Creates the compatibility inventory entry by delegating to InventoryPanel. */
    public inventory(User user) {
        super(user);
    }
}
