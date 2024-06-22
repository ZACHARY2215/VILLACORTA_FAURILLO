import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart extends JFrame {
    private JTextField productNameField, productPriceField, productQuantityField;
    private JTextArea displayArea;
    private JButton addButton, updateButton, deleteButton, clearButton, saveButton, loadButton;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private Map<Integer, Product> cart;
    private int nextItemNumber = 1;
    private JLabel imageLabel;
    private JLabel totalPriceLabel;

    public ShoppingCart() {
        cart = new HashMap<>();

        setTitle("Online Shopping Cart");
        setLayout(new BorderLayout());
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel for input fields
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(3, 2));

        topPanel.add(new JLabel("Product Name:"));
        productNameField = new JTextField();
        topPanel.add(productNameField);

        topPanel.add(new JLabel("Product Price:"));
        productPriceField = new JTextField();
        topPanel.add(productPriceField);

        topPanel.add(new JLabel("Product Quantity:"));
        productQuantityField = new JTextField();
        topPanel.add(productQuantityField);

        // Bottom Panel for buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 5));

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");

        bottomPanel.add(addButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(loadButton);

        // Center Panel for table and image
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        // Table for items
        String[] columnNames = { "Item Number", "Name", "Price", "Quantity", "Total" };
        tableModel = new DefaultTableModel(columnNames, 0);
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                displaySelectedItem();
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(itemTable);

        // Load an image
        imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("path/to/your/image.jpg"); // Replace with the path to your image file
        imageLabel.setIcon(imageIcon);

        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(imageLabel, BorderLayout.EAST);

        // Total price label
        totalPriceLabel = new JLabel("Total Price: $0.00", JLabel.CENTER);
        centerPanel.add(totalPriceLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateProduct();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteProduct();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveCart();
            }
        });
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadCart();
            }
        });

        setVisible(true);
    }

    private void addProduct() {
        String name = productNameField.getText().trim();
        String priceStr = productPriceField.getText().trim();
        String quantityStr = productQuantityField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            showMessage("All fields must be filled", "Error");
            return;
        }

        if (isDuplicateName(name)) {
            showMessage("Product with the same name already exists", "Error");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            Product product = new Product(nextItemNumber, name, price, quantity);
            cart.put(nextItemNumber, product);
            tableModel.addRow(new Object[] { product.getItemNumber(), product.getName(), product.getPrice(),
                    product.getQuantity(), product.getTotal() });
            updateTotalPrice();
            nextItemNumber++;
            showMessage("Product added successfully", "Success");
            clearFields();
        } catch (NumberFormatException e) {
            showMessage("Price and Quantity must be numeric", "Error");
        }
    }

    private void updateProduct() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("No item selected", "Error");
            return;
        }

        String name = productNameField.getText().trim();
        String priceStr = productPriceField.getText().trim();
        String quantityStr = productQuantityField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            showMessage("All fields must be filled", "Error");
            return;
        }

        int itemNumber = (int) tableModel.getValueAt(selectedRow, 0);
        Product existingProduct = cart.get(itemNumber);
        if (!existingProduct.getName().equals(name) && isDuplicateName(name)) {
            showMessage("Product with the same name already exists", "Error");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            Product product = new Product(itemNumber, name, price, quantity);
            cart.put(itemNumber, product);
            tableModel.setValueAt(name, selectedRow, 1);
            tableModel.setValueAt(price, selectedRow, 2);
            tableModel.setValueAt(quantity, selectedRow, 3);
            tableModel.setValueAt(product.getTotal(), selectedRow, 4);
            updateTotalPrice();
            showMessage("Product updated successfully", "Success");
            clearFields();
        } catch (NumberFormatException e) {
            showMessage("Price and Quantity must be numeric", "Error");
        }
    }

    private void deleteProduct() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("No item selected", "Error");
            return;
        }

        int itemNumber = (int) tableModel.getValueAt(selectedRow, 0);
        cart.remove(itemNumber);
        tableModel.removeRow(selectedRow);
        renumberItems(); // Adjust item numbers after deletion
        updateTotalPrice();
        showMessage("Product deleted successfully", "Success");
        clearFields();
    }

    private void renumberItems() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(i + 1, i, 0); // Update item number in the table
            int itemNumber = (int) tableModel.getValueAt(i, 0);
            Product product = cart.get(itemNumber);
            if (product != null) {
                product.setItemNumber(i + 1); // Update item number in the cart map
            }
        }
        nextItemNumber = tableModel.getRowCount() + 1; // Update next item number
    }

    private void saveCart() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("cart.txt"))) {
            for (Product product : cart.values()) {
                writer.write(product.toCSV());
                writer.newLine();
            }
            showMessage("Cart saved successfully", "Success");
        } catch (IOException e) {
            showMessage("Error saving cart: " + e.getMessage(), "Error");
        }
    }

    private void loadCart() {
        cart.clear();
        tableModel.setRowCount(0);
        nextItemNumber = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader("cart.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4)
                    continue;
                int itemNumber = Integer.parseInt(parts[0]);
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);
                int quantity = Integer.parseInt(parts[3]);
                Product product = new Product(itemNumber, name, price, quantity);
                cart.put(itemNumber, product);
                tableModel.addRow(new Object[] { product.getItemNumber(), product.getName(), product.getPrice(),
                        product.getQuantity(), product.getTotal() });
                nextItemNumber++;
            }
            renumberItems(); // Adjust item numbers after loading
            updateTotalPrice();
            showMessage("Cart loaded successfully", "Success");
        } catch (IOException e) {
            showMessage("Error loading cart: " + e.getMessage(), "Error");
        }
    }

    private void updateTotalPrice() {
        double totalPrice = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double total = (double) tableModel.getValueAt(i, 2) * (int) tableModel.getValueAt(i, 3);
            tableModel.setValueAt(total, i, 4);
            totalPrice += total;
        }
        totalPriceLabel.setText("Total Price: $" + String.format("%.2f", totalPrice));
    }

    private void clearFields() {
        productNameField.setText("");
        productPriceField.setText("");
        productQuantityField.setText("");
    }

    private void displaySelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1)
            return;

        int itemNumber = (int) tableModel.getValueAt(selectedRow, 0);
        Product product = cart.get(itemNumber);
        if (product != null) {
            productNameField.setText(product.getName());
            productPriceField.setText(String.valueOf(product.getPrice()));
            productQuantityField.setText(String.valueOf(product.getQuantity()));
        }
    }

    private boolean isDuplicateName(String name) {
        return cart.values().stream().anyMatch(p -> p.getName().equals(name));
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ShoppingCart();
            }
        });
    }
}

class Product {
    private int itemNumber;
    private String name;
    private double price;
    private int quantity;

    public Product(int itemNumber, String name, double price, int quantity) {
        this.itemNumber = itemNumber;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return String.format("Item Number: %d | Name: %s | Price: $%.2f | Quantity: %d", itemNumber, name, price,
                quantity);
    }

    public String toCSV() {
        return String.join(",", String.valueOf(itemNumber), name, String.valueOf(price), String.valueOf(quantity));
    }
}
