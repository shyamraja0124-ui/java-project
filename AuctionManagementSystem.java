import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class AuctionManagementSystem extends JFrame implements ActionListener {

    static class AuctionItem {
        int id;
        String name, category, highestBidder, closingDate, status, feedback;
        double startingPrice, highestBid;
        int rating;

        AuctionItem(int id, String name, String category,
                    double startingPrice, String closingDate) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.startingPrice = startingPrice;
            this.highestBid = startingPrice;
            this.highestBidder = "No Bidder";
            this.closingDate = closingDate;
            this.status = "Active";
            this.feedback = "No Feedback";
            this.rating = 0;
        }
    }

    // Modern High-Contrast Executive Color Palette
    Color primaryDark    = new Color(15, 23, 42);      // Slate 900 (Header / Dark surfaces)
    Color primaryIndigo  = new Color(30, 41, 59);      // Slate 800
    Color bgCanvas       = new Color(241, 245, 249);   // Slate 100 (Clean canvas)
    Color surfaceCard    = Color.WHITE;                // Surface cards
    Color borderLight    = new Color(226, 232, 240);   // Slate 200 (Subtle borders)
    Color borderField    = new Color(203, 213, 225);   // Slate 300 (Input borders)
    Color focusColor     = new Color(37, 99, 235);     // Blue 600 (Focus ring)

    Color textDark       = new Color(15, 23, 42);      // Slate 900 (High-contrast text)
    Color textMuted      = new Color(100, 116, 139);   // Slate 500 (Captions / metadata)
    Color textSubtle     = new Color(148, 163, 184);   // Slate 400

    Color accentBlue     = new Color(37, 99, 235);     // Royal Blue 600
    Color accentGreen    = new Color(5, 150, 105);     // Emerald 600 (High contrast on white)
    Color accentRed      = new Color(220, 38, 38);     // Crimson 600 (High contrast on white)
    Color accentCopper   = new Color(194, 65, 12);     // Amber/Copper 700 (High contrast on white)
    Color accentPurple   = new Color(124, 58, 237);    // Violet 600
    Color accentBronze   = new Color(180, 83, 9);      // Bronze/Amber 700
    Color accentDarkRed  = new Color(153, 27, 27);     // Dark Red 800
    Color accentSlate    = new Color(71, 85, 105);     // Slate 600

    Font headingFont     = new Font("Segoe UI", Font.BOLD, 17);
    Font normalFont      = new Font("Segoe UI", Font.PLAIN, 13);
    Font labelFont       = new Font("Segoe UI", Font.BOLD, 11);
    Font monoFont        = new Font("Consolas", Font.PLAIN, 12);

    JTextField tfName     = createStyledTextField();
    JTextField tfPrice    = createStyledTextField();
    JTextField tfBidder   = createStyledTextField();
    JTextField tfBid      = createStyledTextField();
    JTextField tfDate     = createStyledTextField();
    JTextField tfSearch   = createStyledTextField();
    JTextField tfFeedback = createStyledTextField();

    JComboBox<String> chCategory = new JComboBox<>(new String[]{
        "Electronics", "Vehicles", "Furniture",
        "Antiques", "Collectibles", "Books", "Other"
    });

    JComboBox<String> chRating = new JComboBox<>(
        new String[]{"1", "2", "3", "4", "5"}
    );

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> itemList = new JList<>(listModel);

    JTextArea taInfo = new JTextArea();

    JLabel totalLabel  = new JLabel("0");
    JLabel activeLabel = new JLabel("0");
    JLabel closedLabel = new JLabel("0");

    ArrayList<AuctionItem> auctions = new ArrayList<>();
    ArrayList<AuctionItem> shown = new ArrayList<>();

    static final String FILE = "auctiondata.txt";
    int nextId = 1;

    JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(textDark);
        tf.setCaretColor(accentBlue);
        tf.setBackground(new Color(248, 250, 252));
        tf.setBorder(new CompoundBorder(
            new LineBorder(borderField, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBackground(Color.WHITE);
                tf.setBorder(new CompoundBorder(
                    new LineBorder(focusColor, 2, true),
                    new EmptyBorder(5, 9, 5, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBackground(new Color(248, 250, 252));
                tf.setBorder(new CompoundBorder(
                    new LineBorder(borderField, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
                ));
            }
        });
        return tf;
    }

    void styleComboBoxes() {
        chCategory.setFont(normalFont);
        chCategory.setForeground(textDark);
        chCategory.setBackground(Color.WHITE);
        chCategory.setBorder(new CompoundBorder(
            new LineBorder(borderField, 1, true),
            new EmptyBorder(3, 6, 3, 6)
        ));
        chCategory.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(5, 8, 5, 8));
                return l;
            }
        });

        chRating.setFont(normalFont);
        chRating.setForeground(textDark);
        chRating.setBackground(Color.WHITE);
        chRating.setBorder(new CompoundBorder(
            new LineBorder(borderField, 1, true),
            new EmptyBorder(3, 6, 3, 6)
        ));
        chRating.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    l.setText(value + " / 5 Stars");
                }
                l.setBorder(new EmptyBorder(5, 8, 5, 8));
                return l;
            }
        });
    }

    class ModernCard extends JPanel {
        int radius;
        Color cardColor;
        Color outlineColor;

        ModernCard(Color color, int radius) {
            this(color, radius, borderLight);
        }

        ModernCard(Color color, int radius, Color outline) {
            this.radius = radius;
            this.cardColor = color;
            this.outlineColor = outline;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(cardColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            if (outlineColor != null) {
                g2.setColor(outlineColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        Color baseColor;

        ModernButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color current = baseColor;
            if (getModel().isPressed()) {
                current = baseColor.darker().darker();
            } else if (getModel().isRollover()) {
                current = baseColor.darker();
            }

            g2.setColor(current);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public AuctionManagementSystem() {
        setTitle("Auction Management System");
        setSize(1260, 880);
        setMinimumSize(new Dimension(1080, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(bgCanvas);
        setLayout(new BorderLayout());

        styleComboBoxes();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });

        // Press Enter in search field to search
        tfSearch.addActionListener(e -> searchAuction());

        load();
        showAll();

        setVisible(true);
    }

    JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primaryDark);
        header.setBorder(new EmptyBorder(14, 28, 14, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandRow.setOpaque(false);

        JLabel title = new JLabel("AUCTION MANAGEMENT SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        brandRow.add(title);

        JLabel subtitle = new JLabel("Live Bidding Administration, Catalog Management & Real-Time Analytics");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setBorder(new EmptyBorder(2, 8, 0, 0));

        titlePanel.add(brandRow);
        titlePanel.add(subtitle);

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tagPanel.setOpaque(false);

        JLabel statusBadge = new JLabel("[ LIVE ] SYSTEM READY");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(new Color(52, 211, 153));
        statusBadge.setBorder(new CompoundBorder(
            new LineBorder(new Color(52, 211, 153), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel adminBadge = new JLabel("ADMIN PORTAL");
        adminBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        adminBadge.setForeground(Color.WHITE);
        adminBadge.setBorder(new CompoundBorder(
            new LineBorder(new Color(100, 116, 139), 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));

        tagPanel.add(statusBadge);
        tagPanel.add(adminBadge);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(tagPanel, BorderLayout.EAST);

        return header;
    }

    JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(14, 14));
        main.setBackground(bgCanvas);
        main.setBorder(new EmptyBorder(14, 20, 16, 20));

        main.add(createStatistics(), BorderLayout.NORTH);

        JPanel workspace = new JPanel(new BorderLayout(16, 0));
        workspace.setOpaque(false);

        workspace.add(createFormPanel(), BorderLayout.WEST);
        workspace.add(createAuctionPanel(), BorderLayout.CENTER);

        main.add(workspace, BorderLayout.CENTER);
        main.add(createBottomPanel(), BorderLayout.SOUTH);

        return main;
    }

    JPanel createStatistics() {
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);

        stats.add(createStatCard("TOTAL AUCTIONS", totalLabel, accentBlue, "All inventory items registered"));
        stats.add(createStatCard("ACTIVE AUCTIONS", activeLabel, accentGreen, "Open for bidding & offers"));
        stats.add(createStatCard("CLOSED AUCTIONS", closedLabel, accentRed, "Finalized & winning bids locked"));

        return stats;
    }

    JPanel createStatCard(String name, JLabel value, Color accent, String subtext) {
        JPanel card = new ModernCard(surfaceCard, 14);
        card.setLayout(new BorderLayout(6, 4));
        card.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(textMuted);

        value.setFont(new Font("Segoe UI", Font.BOLD, 26));
        value.setForeground(accent);

        JLabel sub = new JLabel(subtext);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(textSubtle);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(value, BorderLayout.NORTH);
        centerPanel.add(sub, BorderLayout.SOUTH);

        card.add(label, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    JPanel createFormPanel() {
        JPanel panel = new ModernCard(surfaceCard, 14);
        panel.setLayout(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(440, 0));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel headerBox = new JPanel(new BorderLayout());
        headerBox.setOpaque(false);

        JLabel heading = new JLabel("Auction Details");
        heading.setFont(headingFont);
        heading.setForeground(textDark);

        JLabel subHead = new JLabel("Enter item information or modify selected auction");
        subHead.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subHead.setForeground(textMuted);

        headerBox.add(heading, BorderLayout.NORTH);
        headerBox.add(subHead, BorderLayout.SOUTH);

        // Responsive Form Fields Container
        JPanel formBody = new JPanel();
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));
        formBody.setOpaque(false);

        // Row 1: Item Name (Full Width - 400px+ space for long product names)
        formBody.add(createFieldBlock("Item Name", tfName, 34));
        formBody.add(Box.createVerticalStrut(6));

        // Row 2: Category & Starting Price (Pair - ample room for dropdown & currency input)
        JPanel pair1 = new JPanel(new GridLayout(1, 2, 10, 0));
        pair1.setOpaque(false);
        pair1.add(createFieldBlock("Category", chCategory, 34));
        pair1.add(createFieldBlock("Starting Price (Rs.)", tfPrice, 34));
        formBody.add(pair1);
        formBody.add(Box.createVerticalStrut(6));

        // Row 3: Closing Date & Rating (Pair - clean alignment)
        JPanel pair2 = new JPanel(new GridLayout(1, 2, 10, 0));
        pair2.setOpaque(false);
        pair2.add(createFieldBlock("Closing Date (YYYY-MM-DD)", tfDate, 34));
        pair2.add(createFieldBlock("Rating (1-5 Stars)", chRating, 34));
        formBody.add(pair2);
        formBody.add(Box.createVerticalStrut(6));

        // Row 4: Bidder Name & Bid Amount (Pair - clear separation for bidding entries)
        JPanel pair3 = new JPanel(new GridLayout(1, 2, 10, 0));
        pair3.setOpaque(false);
        pair3.add(createFieldBlock("Bidder Name", tfBidder, 34));
        pair3.add(createFieldBlock("Bid Amount (Rs.)", tfBid, 34));
        formBody.add(pair3);
        formBody.add(Box.createVerticalStrut(6));

        // Row 5: Feedback (Full Width - comfortably view and write complete sentences)
        formBody.add(createFieldBlock("Feedback / Buyer Remarks", tfFeedback, 34));

        // Wrap form in smooth scrollpane for responsiveness on low-resolution displays
        JScrollPane scrollForm = new JScrollPane(formBody);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.getVerticalScrollBar().setUnitIncrement(14);

        // Action buttons inside form
        JButton addButton = createButton("+ Add Auction", accentGreen, "Add Auction");
        JButton clearButton = createButton("Clear Form", accentSlate, "Clear Form");

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(0, 38));
        actions.add(addButton);
        actions.add(clearButton);

        panel.add(headerBox, BorderLayout.NORTH);
        panel.add(scrollForm, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    JPanel createFieldBlock(String labelText, JComponent comp, int compHeight) {
        JPanel block = new JPanel(new BorderLayout(0, 3));
        block.setOpaque(false);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, compHeight + 20));

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(textDark);

        comp.setPreferredSize(new Dimension(100, compHeight));

        block.add(label, BorderLayout.NORTH);
        block.add(comp, BorderLayout.CENTER);
        return block;
    }

    JPanel createAuctionPanel() {
        JPanel panel = new ModernCard(surfaceCard, 14);
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGrp.setOpaque(false);

        JLabel heading = new JLabel("Auction Inventory");
        heading.setFont(headingFont);
        heading.setForeground(textDark);

        JLabel subtitle = new JLabel("Live catalog & history");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(textMuted);

        titleGrp.add(heading);
        titleGrp.add(subtitle);

        tfSearch.setPreferredSize(new Dimension(180, 34));

        JButton searchButton = createButton("Search", accentBlue, "Search");
        JButton viewButton   = createButton("View All", accentSlate, "View All");

        searchButton.setPreferredSize(new Dimension(85, 34));
        viewButton.setPreferredSize(new Dimension(85, 34));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(tfSearch);
        searchPanel.add(searchButton);
        searchPanel.add(viewButton);

        top.add(titleGrp, BorderLayout.WEST);
        top.add(searchPanel, BorderLayout.EAST);

        itemList.setFont(normalFont);
        itemList.setFixedCellHeight(52);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setBackground(surfaceCard);
        itemList.setSelectionBackground(new Color(239, 246, 255));

        itemList.setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {

                JPanel cell = new JPanel(new BorderLayout(12, 0));
                cell.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, borderLight),
                    new EmptyBorder(6, 10, 6, 10)
                ));

                if (isSelected) {
                    cell.setBackground(new Color(239, 246, 255));
                    cell.setBorder(new CompoundBorder(
                        new MatteBorder(0, 4, 1, 0, accentBlue),
                        new EmptyBorder(6, 6, 6, 10)
                    ));
                } else {
                    cell.setBackground(index % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                if (index >= 0 && index < shown.size()) {
                    AuctionItem item = shown.get(index);

                    // Left badge with ID
                    JPanel idBadge = new JPanel(new GridBagLayout());
                    idBadge.setPreferredSize(new Dimension(38, 30));
                    idBadge.setOpaque(false);
                    JLabel idLabel = new JLabel("#" + item.id);
                    idLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    idLabel.setForeground(accentBlue);
                    idBadge.add(idLabel);

                    // Center: Name and Category / Closing Date
                    JPanel center = new JPanel(new GridLayout(2, 1, 0, 1));
                    center.setOpaque(false);

                    JLabel nameLbl = new JLabel(item.name);
                    nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    nameLbl.setForeground(textDark);

                    JLabel metaLbl = new JLabel(item.category + "  |  Closes: " + item.closingDate + "  |  Bidder: " + item.highestBidder);
                    metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    metaLbl.setForeground(textMuted);

                    center.add(nameLbl);
                    center.add(metaLbl);

                    // Right: Price & Status Pill
                    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
                    right.setOpaque(false);

                    JLabel priceLbl = new JLabel(String.format("Rs. %.2f", item.highestBid));
                    priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    priceLbl.setForeground(accentGreen);

                    JLabel statusPill = new JLabel("  " + item.status.toUpperCase() + "  ");
                    statusPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    boolean isActive = item.status.equalsIgnoreCase("Active");
                    statusPill.setForeground(isActive ? new Color(5, 150, 105) : new Color(220, 38, 38));
                    statusPill.setBackground(isActive ? new Color(209, 250, 229) : new Color(254, 226, 226));
                    statusPill.setOpaque(true);
                    statusPill.setBorder(new LineBorder(isActive ? new Color(167, 243, 208) : new Color(254, 202, 202), 1, true));

                    right.add(priceLbl);
                    right.add(statusPill);

                    cell.add(idBadge, BorderLayout.WEST);
                    cell.add(center, BorderLayout.CENTER);
                    cell.add(right, BorderLayout.EAST);
                } else {
                    JLabel fallback = new JLabel(value != null ? value : "");
                    fallback.setFont(normalFont);
                    cell.add(fallback, BorderLayout.CENTER);
                }

                return cell;
            }
        });

        JScrollPane scroll = new JScrollPane(itemList);
        scroll.setBorder(new LineBorder(borderLight, 1, true));
        scroll.getViewport().setBackground(surfaceCard);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel hint = new JLabel("Tip: Select any item above to view details, place bids, or record reviews.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(textMuted);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(hint, BorderLayout.WEST);

        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedItem();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // 2 Clean, Perfectly Balanced Rows of Action Buttons
        JPanel buttonsContainer = new JPanel(new GridLayout(2, 1, 0, 8));
        buttonsContainer.setOpaque(false);

        // Row 1: Bidding & Direct Item Actions (5 buttons evenly divided)
        JPanel row1 = new JPanel(new GridLayout(1, 5, 8, 0));
        row1.setOpaque(false);
        row1.setPreferredSize(new Dimension(0, 35));
        addActionButton(row1, "Place Bid", accentGreen);
        addActionButton(row1, "Close Auction", accentRed);
        addActionButton(row1, "Feedback & Rating", accentCopper);
        addActionButton(row1, "Recommendation", accentBlue);
        addActionButton(row1, "Compare Auctions", accentPurple);

        // Row 2: Analytics, Reminders & Management (4 buttons evenly divided)
        JPanel row2 = new JPanel(new GridLayout(1, 4, 8, 0));
        row2.setOpaque(false);
        row2.setPreferredSize(new Dimension(0, 35));
        addActionButton(row2, "Bid Reminder", accentBronze);
        addActionButton(row2, "Delete Auction", accentDarkRed);
        addActionButton(row2, "Refresh", accentSlate);
        addActionButton(row2, "Exit", primaryIndigo);

        buttonsContainer.add(row1);
        buttonsContainer.add(row2);

        // System Activity & Reports Console
        JPanel consolePanel = new ModernCard(new Color(15, 23, 42), 12, null); // Slate 900
        consolePanel.setLayout(new BorderLayout(0, 4));
        consolePanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel consoleHeader = new JPanel(new BorderLayout());
        consoleHeader.setOpaque(false);

        JLabel consoleTitle = new JLabel("ACTIVITY CONSOLE & SYSTEM NOTIFICATIONS");
        consoleTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        consoleTitle.setForeground(new Color(148, 163, 184));

        JLabel consoleDot = new JLabel("[ ONLINE ]");
        consoleDot.setFont(new Font("Segoe UI", Font.BOLD, 10));
        consoleDot.setForeground(new Color(52, 211, 153));

        consoleHeader.add(consoleTitle, BorderLayout.WEST);
        consoleHeader.add(consoleDot, BorderLayout.EAST);

        taInfo.setEditable(false);
        taInfo.setFont(monoFont);
        taInfo.setForeground(new Color(241, 245, 249));
        taInfo.setBackground(new Color(15, 23, 42));
        taInfo.setCaretColor(new Color(56, 189, 248));
        taInfo.setLineWrap(true);
        taInfo.setWrapStyleWord(true);
        taInfo.setBorder(new EmptyBorder(4, 4, 4, 4));
        taInfo.setText("Welcome! Enter auction details on the left, or select an existing item to bid.");

        JScrollPane infoScroll = new JScrollPane(taInfo);
        infoScroll.setPreferredSize(new Dimension(100, 85));
        infoScroll.setBorder(null);
        infoScroll.getViewport().setBackground(new Color(15, 23, 42));
        infoScroll.getVerticalScrollBar().setUnitIncrement(14);

        consolePanel.add(consoleHeader, BorderLayout.NORTH);
        consolePanel.add(infoScroll, BorderLayout.CENTER);

        panel.add(buttonsContainer, BorderLayout.NORTH);
        panel.add(consolePanel, BorderLayout.CENTER);

        return panel;
    }

    void addActionButton(JPanel panel, String name, Color color) {
        JButton button = createButton(name, color, name);
        panel.add(button);
    }

    JButton createButton(String text, Color color, String command) {
        JButton button = new ModernButton(text, color);
        button.setActionCommand(command);
        button.addActionListener(this);
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "Add Auction":
                addAuction();
                break;
            case "Clear Form":
                clearForm();
                break;
            case "Search":
                searchAuction();
                break;
            case "View All":
            case "Refresh":
                showAll();
                taInfo.setText("All auction items displayed.");
                break;
            case "Place Bid":
                placeBid();
                break;
            case "Recommendation":
                recommendation();
                break;
            case "Compare Auctions":
                compareAuctions();
                break;
            case "Feedback & Rating":
                addFeedback();
                break;
            case "Bid Reminder":
                bidReminder();
                break;
            case "Close Auction":
                closeAuction();
                break;
            case "Delete Auction":
                deleteAuction();
                break;
            case "Exit":
                exitApp();
                break;
        }
    }

    void addAuction() {
        String name = tfName.getText().trim();
        String price = tfPrice.getText().trim();
        String date = tfDate.getText().trim();

        if (name.isEmpty() || price.isEmpty() || date.isEmpty()) {
            taInfo.setText("Please enter all auction details.");
            return;
        }

        try {
            double amount = Double.parseDouble(price);

            if (amount <= 0) {
                taInfo.setText("Starting price must be greater than zero.");
                return;
            }

            AuctionItem x = new AuctionItem(
                nextId++,
                name,
                (String) chCategory.getSelectedItem(),
                amount,
                date
            );

            auctions.add(x);

            save();
            showAll();
            clearForm();

            taInfo.setText("Auction item added successfully.");

        } catch (NumberFormatException ex) {
            taInfo.setText("Enter a valid starting price.");
        }
    }

    void showAll() {
        shown.clear();
        shown.addAll(auctions);
        refreshList();
    }

    void placeBid() {
        int i = itemList.getSelectedIndex();

        if (i < 0) {
            taInfo.setText("Select an auction item.");
            return;
        }

        AuctionItem x = shown.get(i);

        if (!x.status.equals("Active")) {
            taInfo.setText("This auction is already closed.");
            return;
        }

        String bidder = tfBidder.getText().trim();
        String amount = tfBid.getText().trim();

        if (bidder.isEmpty() || amount.isEmpty()) {
            taInfo.setText("Enter bidder name and bid amount.");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(amount);

            if (bidAmount <= x.highestBid) {
                taInfo.setText("Bid must be greater than current highest bid.");
                return;
            }

            x.highestBid = bidAmount;
            x.highestBidder = bidder;

            save();
            showAll();

            taInfo.setText(
                "BID PLACED SUCCESSFULLY!\n\n" +
                "Item: " + x.name +
                "\nHighest Bidder: " + x.highestBidder +
                "\nHighest Bid: " + x.highestBid
            );

        } catch (NumberFormatException ex) {
            taInfo.setText("Enter a valid bid amount.");
        }
    }

    void searchAuction() {
        String key = tfSearch.getText().trim().toLowerCase();
        shown.clear();

        for (AuctionItem x : auctions) {
            if (x.name.toLowerCase().contains(key) ||
                x.category.toLowerCase().contains(key) ||
                x.status.toLowerCase().contains(key)) {
                shown.add(x);
            }
        }

        refreshList();
        taInfo.setText("SEARCH RESULTS\nItems Found: " + shown.size());
    }

    void recommendation() {
        int i = itemList.getSelectedIndex();

        if (i < 0) {
            taInfo.setText("Select an item to get recommendations.");
            return;
        }

        AuctionItem selected = shown.get(i);
        StringBuilder sb = new StringBuilder();

        sb.append("===== RECOMMENDED ITEMS =====\n\n");

        int count = 0;

        for (AuctionItem x : auctions) {
            if (x.id != selected.id &&
                x.category.equals(selected.category) &&
                x.status.equals("Active")) {

                sb.append("Item: ").append(x.name)
                  .append("\nCategory: ").append(x.category)
                  .append("\nStarting Price: ").append(x.startingPrice)
                  .append("\nHighest Bid: ").append(x.highestBid)
                  .append("\n\n");

                count++;
            }
        }

        if (count == 0) {
            sb.append("No similar active items found.");
        }

        taInfo.setText(sb.toString());
    }

    void compareAuctions() {
        StringBuilder sb = new StringBuilder();

        sb.append("===== AUCTION COMPARISON =====\n\n");

        if (auctions.size() < 2) {
            taInfo.setText("Add at least two items for comparison.");
            return;
        }

        for (AuctionItem x : auctions) {
            sb.append("Item ID: ").append(x.id)
              .append("\nItem Name: ").append(x.name)
              .append("\nCategory: ").append(x.category)
              .append("\nStarting Price: ").append(x.startingPrice)
              .append("\nHighest Bid: ").append(x.highestBid)
              .append("\nStatus: ").append(x.status)
              .append("\n-----------------------------\n");
        }

        taInfo.setText(sb.toString());
    }

    void addFeedback() {
        int i = itemList.getSelectedIndex();

        if (i < 0) {
            taInfo.setText("Select an auction item.");
            return;
        }

        String feedback = tfFeedback.getText().trim();

        if (feedback.isEmpty()) {
            taInfo.setText("Enter your feedback.");
            return;
        }

        AuctionItem x = shown.get(i);

        x.feedback = feedback;
        x.rating = Integer.parseInt((String) chRating.getSelectedItem());

        save();
        showAll();

        taInfo.setText(
            "FEEDBACK SUBMITTED SUCCESSFULLY\n\n" +
            "Item: " + x.name +
            "\nFeedback: " + x.feedback +
            "\nRating: " + x.rating + "/5"
        );
    }

    void bidReminder() {
        StringBuilder sb = new StringBuilder();

        sb.append("===== AUCTION REMINDERS =====\n\n");

        int count = 0;

        for (AuctionItem x : auctions) {
            if (x.status.equals("Active")) {
                sb.append("Item: ").append(x.name)
                  .append("\nClosing Date: ").append(x.closingDate)
                  .append("\nHighest Bid: ").append(x.highestBid)
                  .append("\nStatus: ").append(x.status)
                  .append("\n-------------------------\n");

                count++;
            }
        }

        if (count == 0) {
            sb.append("No active auctions available.");
        }

        taInfo.setText(sb.toString());
    }

    void closeAuction() {
        int i = itemList.getSelectedIndex();

        if (i < 0) {
            taInfo.setText("Select an auction item to close.");
            return;
        }

        AuctionItem x = shown.get(i);

        if (x.status.equals("Closed")) {
            taInfo.setText("Auction is already closed.");
            return;
        }

        x.status = "Closed";

        save();
        showAll();

        taInfo.setText(
            "===== AUCTION CLOSED =====\n\n" +
            "Item: " + x.name +
            "\nWinner: " + x.highestBidder +
            "\nWinning Bid: " + x.highestBid
        );
    }

    void deleteAuction() {
        int i = itemList.getSelectedIndex();

        if (i < 0) {
            taInfo.setText("Select an item to delete.");
            return;
        }

        AuctionItem x = shown.get(i);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete " + x.name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        auctions.remove(x);

        save();
        showAll();
        clearForm();

        taInfo.setText("Auction item deleted successfully.");
    }

    void displaySelectedItem() {
        int i = itemList.getSelectedIndex();

        if (i < 0 || i >= shown.size()) {
            return;
        }

        AuctionItem x = shown.get(i);

        tfName.setText(x.name);
        chCategory.setSelectedItem(x.category);
        tfPrice.setText(String.valueOf(x.startingPrice));
        tfDate.setText(x.closingDate);
        tfBidder.setText(x.highestBidder);
        tfBid.setText(String.valueOf(x.highestBid));

        tfFeedback.setText(x.feedback.equals("No Feedback") ? "" : x.feedback);

        chRating.setSelectedItem(String.valueOf(x.rating == 0 ? 1 : x.rating));
    }

    void refreshList() {
        listModel.clear();

        for (AuctionItem x : shown) {
            listModel.addElement(
                String.format(
                    "%-4s | %-20s | %-15s | Rs. %-10.2f | %s",
                    x.id,
                    x.name.length() > 20 ? x.name.substring(0, 20) : x.name,
                    x.category,
                    x.highestBid,
                    x.status
                )
            );
        }

        updateStatistics();
    }

    void updateStatistics() {
        int active = 0;
        int closed = 0;

        for (AuctionItem x : auctions) {
            if (x.status.equals("Active")) {
                active++;
            } else {
                closed++;
            }
        }

        totalLabel.setText(String.valueOf(auctions.size()));
        activeLabel.setText(String.valueOf(active));
        closedLabel.setText(String.valueOf(closed));
    }

    void clearForm() {
        tfName.setText("");
        tfPrice.setText("");
        tfDate.setText("");
        tfBidder.setText("");
        tfBid.setText("");
        tfSearch.setText("");
        tfFeedback.setText("");

        chCategory.setSelectedIndex(0);
        chRating.setSelectedIndex(0);

        itemList.clearSelection();
    }

    void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (AuctionItem x : auctions) {
                pw.println(
                    x.id + "\t" +
                    x.name + "\t" +
                    x.category + "\t" +
                    x.startingPrice + "\t" +
                    x.highestBid + "\t" +
                    x.highestBidder + "\t" +
                    x.closingDate + "\t" +
                    x.status + "\t" +
                    x.rating + "\t" +
                    x.feedback
                );
            }
        } catch (IOException ex) {
            taInfo.setText("Unable to save auction data.");
        }
    }

    void load() {
        File f = new File(FILE);
        if (!f.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\t", 10);
                if (p.length == 10) {
                    AuctionItem x = new AuctionItem(
                        Integer.parseInt(p[0]),
                        p[1],
                        p[2],
                        Double.parseDouble(p[3]),
                        p[6]
                    );

                    x.highestBid = Double.parseDouble(p[4]);
                    x.highestBidder = p[5];
                    x.status = p[7];
                    x.rating = Integer.parseInt(p[8]);
                    x.feedback = p[9];

                    auctions.add(x);

                    if (x.id >= nextId) {
                        nextId = x.id + 1;
                    }
                }
            }
        } catch (Exception ex) {
            taInfo.setText("Unable to load auction data.");
        }
    }

    void exitApp() {
        save();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            new AuctionManagementSystem();
        });
    }
}
