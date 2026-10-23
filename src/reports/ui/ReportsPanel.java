package reports.ui;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import shared.model.User;
import billing.service.SalesService;
import shared.ui.Ui;

/** Displays daily sales totals and allows administrators to view or print receipts. */
public class ReportsPanel extends JPanel {
    // Administrator identity, report service, date input, totals, and result table.
    private final User admin;
    private final SalesService service = new SalesService();
    private final JTextField date = new JTextField(LocalDate.now().toString(), 12);
    private final JLabel total = new JLabel("Daily total: —");
    private final DefaultTableModel model = new DefaultTableModel(new String[] { "Invoice", "Date/time", "Cashier",
            "Subtotal", "Discount", "Tax", "Total", "Method", "Paid" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    /** Builds the daily report controls and receipt interaction. */
    public ReportsPanel(User user) {
        super(new BorderLayout(8, 8));
        admin = user;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Date (YYYY-MM-DD)"));
        top.add(date);
        JButton load = new JButton("Load sales");
        top.add(load);
        top.add(total);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        // Reload when the date button is clicked or a sale row is double-clicked.
        load.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0)
                        reprint(row);
                }
            }
        });
        refresh();
    }

    /** Parses the selected date and refreshes the sales list and daily total. */
    private void refresh() {
        try {
            // Convert the input to a date before requesting report data.
            LocalDate d = LocalDate.parse(date.getText().trim());
            List<String[]> rows = service.salesForDate(admin, d);
            model.setRowCount(0);
            for (String[] r : rows)
                model.addRow(r);
            total.setText("Daily total: " + service.totalForDate(admin, d));
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    /** Loads the selected invoice receipt and sends it to the print dialog. */
    private void reprint(int row) {
        try {
            String receipt = service.receipt(admin, model.getValueAt(table.convertRowIndexToModel(row), 0).toString());
            JTextArea area = new JTextArea(receipt, 24, 44);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(area), "Receipt",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                area.print();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }
}
