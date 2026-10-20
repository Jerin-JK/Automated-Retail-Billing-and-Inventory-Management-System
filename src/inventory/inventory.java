package inventory;

import shared.model.User;
import inventory.ui.InventoryPanel;

/** Compatibility entry for the original inventory module path. */
@SuppressWarnings("serial")
public class inventory extends InventoryPanel {
    public inventory(User user) {
        super(user);
    }
}
