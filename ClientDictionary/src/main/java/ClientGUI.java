import javax.swing.*;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;

public class ClientGUI extends JFrame{

    // Swing Components
    private JPanel mainPanel;
    private JLabel clientLabel;
    private JTextField clientText;
    private JTextArea outputField;
    private JButton queryButton;
    private JButton addButton;
    private JButton removeButton;
    private JButton updateButton;
    private JButton settingsButton;
    private JButton quitButton;
    private JButton clearButton;

    private Client client;
    private ClientMessageHandler clientMessageHandler;
    private String definitions;

    // GUI Properties
    private final int GUI_WIDTH = 600;
    private final int GUI_HEIGHT = 400;
    private final int SETTING_ADDRESS_COLUMNS = 20;
    private final int SETTING_PORT_COLUMNS = 7;
    private final String GUI_TITLE = "Dictionary";
    private final String WARNING_TITLE = "Warning";
    private final String SETTINGS_TITLE = "Edit Server Settings";
    private final String SETTING_ADDRESS_LABEL = "Host Address:";
    private final String SETTING_PORT_LABEL = "Port:";
    private final String EMPTY_FIELD = "";

    // GUI Alert Messages
    private final String ADD_SUCCESS = "Added successfully!";
    private final String UPDATE_SUCCESS = "Updated successfully!";
    private final String REMOVE_SUCCESS = "Removed successfully!";
    private final String ACTION_FAIL = "Request unsuccessful! Please try another word.";
    private final String SERVER_FAULTY = "Request unsuccessful! An error occurred at the server side, please try " +
            "again later!";
    private final String INVALID_PORT_NO = "Please enter a valid port number!";
    private final String INVALID_WORD = "Invalid word. Please try again.";
    private final String EMPTY_WORD = "Do not leave the word field empty, please try again!";
    private final String EMPTY_DEFINITION = "Do not leave the definition field empty, please try again!";
    private final String WARNING_UPDATE = "Are you sure you want to update this word?";
    private final String WARNING_REMOVE = "Are you sure you want to remove this word?";
    private final String INPUT_DEFINITION_PROMPT = "Enter the word's definition " +
            "(separate with a '*' to add more than one):";
    private final String SETTINGS = "\nPlease go to the settings and try a different server address and/or port.";

    public ClientGUI (Client client, ClientMessageHandler clientMessageHandler) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setSize(GUI_WIDTH,GUI_HEIGHT);
        this.client = client;
        this.clientMessageHandler = clientMessageHandler;

        // Query
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!clientMessageHandler.isEmptyInput(getText())) {
                    sendRequest(ClientMessageHandler.QUERY_CODE);
                } else {
                    // If empty word field
                    displayAlert(EMPTY_WORD);
                }
            }
        });

        // Add
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clientMessageHandler.isEmptyInput(getText())) {
                    // If empty word field
                    displayAlert(EMPTY_WORD);
                } else {
                    definitions = JOptionPane.showInputDialog(mainPanel, INPUT_DEFINITION_PROMPT, null);
                    if (!clientMessageHandler.isEmptyInput(definitions)) {
                        // If definition field not empty
                        sendRequest(ClientMessageHandler.ADD_CODE);
                    } else {
                        displayAlert(EMPTY_DEFINITION);
                    }
                }
            }
        });

        // Remove
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!clientMessageHandler.isEmptyInput(getText())) {
                    int choice = JOptionPane.showConfirmDialog(mainPanel,WARNING_REMOVE,
                            WARNING_TITLE,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_NO_OPTION ) {
                        sendRequest(ClientMessageHandler.REMOVE_CODE);
                    }
                } else {
                    // If empty word field
                    displayAlert(EMPTY_WORD);
                }
            }
        });

        // Update
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clientMessageHandler.isEmptyInput(getText())) {
                    // If empty word field
                    displayAlert(EMPTY_WORD);
                } else {
                    definitions = JOptionPane.showInputDialog(mainPanel, INPUT_DEFINITION_PROMPT, null);
                    if (!clientMessageHandler.isEmptyInput(definitions)) {
                        // If definition field not empty
                        int choice = JOptionPane.showConfirmDialog(mainPanel, WARNING_UPDATE, WARNING_TITLE,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (choice == JOptionPane.YES_NO_OPTION) {
                            sendRequest(ClientMessageHandler.UPDATE_CODE);
                        }
                    } else {
                        displayAlert(EMPTY_DEFINITION);
                    }
                }
            }
        });

        // Clear
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        // Server Settings
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField addressField = new JTextField(SETTING_ADDRESS_COLUMNS);
                JTextField portField = new JTextField(SETTING_PORT_COLUMNS);
                addressField.setText(clientMessageHandler.getHostAddress());
                portField.setText(String.valueOf(clientMessageHandler.getPortNo()));
                JPanel settingPanel = new JPanel();
                settingPanel.add(new JLabel(SETTING_ADDRESS_LABEL));
                settingPanel.add(addressField);
                settingPanel.add(new JLabel(SETTING_PORT_LABEL));
                settingPanel.add(portField);
                int result = JOptionPane.showConfirmDialog(null, settingPanel,
                        SETTINGS_TITLE, JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    // Updates Server Address and Port Number
                    clientMessageHandler.setHostAddress(addressField.getText());
                    try {
                        // Check port number input is an integer
                        clientMessageHandler.setPortNo(Integer.parseInt(portField.getText()));
                    } catch (Exception ex) {
                        displayAlert(INVALID_PORT_NO);
                    }
                }
            }
        });

        // Quit
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.exitProgram();
            }
        });

        // Terminates program on application window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                client.exitProgram();
            }
        });
    }

    public void initialize() {
        setTitle(GUI_TITLE);
        setVisible(true);
        outputField.setEditable(false);

    }

    private void sendRequest(int actionCode) {
        // Trim removes whitespaces from both ends of the string
        String inputText = getText().trim();
        if (clientMessageHandler.isValidWord(inputText) && !clientMessageHandler.isEmptyInput(inputText)) {

            if (actionCode == ClientMessageHandler.UPDATE_CODE || actionCode == ClientMessageHandler.ADD_CODE) {
                    // Update & Add requests requires definitions
                    // Format -> word:definition
                    inputText += ClientMessageHandler.STRING_SEPARATOR + definitions.trim();
            }
            try {
                // Send the request via message handler
                clientMessageHandler.request(actionCode, inputText.toLowerCase());
                displayRequestStatus(actionCode);
            } catch (InvalidSocketException e) {
                displayAlert(e.getMessage() + SETTINGS);
            }
        } else {
            // Invalid word inputted
            displayAlert(INVALID_WORD);
        }
    }

    private void displayRequestStatus(int actionCode) {
        String requestStatus = clientMessageHandler.getRequestStatus();
        if (requestStatus.equals(clientMessageHandler.REQUEST_SUCCESS)) {
            switch (actionCode)
            {
                case ClientMessageHandler.QUERY_CODE:
                    outputField.setText(clientMessageHandler.getDefinition());
                    break;

                case ClientMessageHandler.ADD_CODE:
                    displayAlert(ADD_SUCCESS);
                    break;

                case ClientMessageHandler.REMOVE_CODE:
                    displayAlert(REMOVE_SUCCESS);
                    break;

                case ClientMessageHandler.UPDATE_CODE:
                    displayAlert(UPDATE_SUCCESS);
                    break;
            }
        } else if (requestStatus.equals(clientMessageHandler.REQUEST_FAILURE)){
            displayAlert(ACTION_FAIL);
        } else {
            // Input output error at the server side
            displayAlert(SERVER_FAULTY);
        }
    }

    private void displayAlert(String message) {
        JOptionPane.showMessageDialog(mainPanel, message);
        refresh();
    }

    private String getText() {
        return clientText.getText();
    }

    public void refresh() {
        clientText.setText(EMPTY_FIELD);
        outputField.setText(EMPTY_FIELD);
        definitions = EMPTY_FIELD;
    }
    
    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }
        
    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
        clientText = new JTextField();
        mainPanel.add(clientText, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        outputField = new JTextArea();
        mainPanel.add(outputField, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 4, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        queryButton = new JButton();
        queryButton.setText("Query");
        mainPanel.add(queryButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateButton = new JButton();
        updateButton.setText("Update");
        mainPanel.add(updateButton, new com.intellij.uiDesigner.core.GridConstraints(1, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        mainPanel.add(addButton, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        mainPanel.add(removeButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clientLabel = new JLabel();
        clientLabel.setText("Enter Word:");
        mainPanel.add(clientLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        quitButton = new JButton();
        quitButton.setText("Quit");
        mainPanel.add(quitButton, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsButton = new JButton();
        settingsButton.setText("Server Settings");
        mainPanel.add(settingsButton, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearButton = new JButton();
        clearButton.setText("Clear");
        mainPanel.add(clearButton, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }       
}

   