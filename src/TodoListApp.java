import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class TodoListApp extends JFrame {
    private List<DefaultListModel<String>> dayModels;
    private List<JList<String>> dayLists;
    private JTabbedPane tabbedPane;
    private JTextField taskInput;

    public TodoListApp() {
        setupFrame();      // Configure the JFrame
        setupComponents(); // Add UI components
        loadTasks();       // Load saved tasks
    }

    private void setupFrame() {
        setTitle("Todo List");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
    }

    private void setupComponents() {
        // Task input field and buttons
        taskInput = new JTextField();
        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");

        // Task list setup
        dayModels = new ArrayList<>();
        dayLists = new ArrayList<>();
        tabbedPane = new JTabbedPane();

        String[] days = {"Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            DefaultListModel<String> model = new DefaultListModel<>();
            JList<String> list = new JList<>(model);
            list.setBackground(Color.BLACK);
            list.setForeground(Color.WHITE);
            dayModels.add(model);
            dayLists.add(list);
            JScrollPane scrollPane = new JScrollPane(list);
            scrollPane.setBackground(Color.BLACK);
            tabbedPane.addTab(day, scrollPane);
        }

        // Input panel (North)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);

        // Button panel (South)
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);

        addButton.setBackground(new Color(34, 139, 34));  // Green background for "Add Task"
        addButton.setForeground(Color.WHITE);  // White text for "Add Task"
        deleteButton.setBackground(new Color(139, 0, 0));  // Red background for "Delete Task"
        deleteButton.setForeground(Color.WHITE);  // White text for "Delete Task"
        inputPanel.setBackground(Color.BLACK);
        tabbedPane.setBackground(Color.WHITE);

        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        taskInput.addActionListener(e -> addTask()); // Enter key support

        // Save tasks on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });
    }

    private void addTask() {
        String task = taskInput.getText().trim();

        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab == -1) return;
        dayModels.get(selectedTab).addElement(task);
        taskInput.setText("");
    }   

    private void deleteTask() {
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab == -1) {
            JOptionPane.showMessageDialog(this, "No Tab Selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JList<String> currentList = dayLists.get(selectedTab);
        int selectedIndex = currentList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "No Task Selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dayModels.get(selectedTab).remove(selectedIndex);
    }

    private void saveTasks() {
        String[] days = {"Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 0; i < days.length; i++) {
            String fileName = days[i] + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

                DefaultListModel<String> model = dayModels.get(i);

                for (int j = 0; j < model.size(); j++) {
                    writer.write(model.getElementAt(j));
                    writer.newLine();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error saving " + days[i] + "'s tasks",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void loadTasks() {
        String[] files = {"Monday.txt", "Tuesday.txt", "Wednesday.txt",
                "Thursday.txt", "Friday.txt", "Saturday.txt", "Sunday.txt"};

        for (int i = 0; i < files.length; i++){
            File taskFile = new File(files[i]);

            if (taskFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(taskFile))) {
                    String line;
                    DefaultListModel<String> model = dayModels.get(i);
                    while ((line = br.readLine()) != null) {
                        model.addElement(line);
                    }
                }catch (IOException e) {
                    // Handle any exceptions
                    JOptionPane.showMessageDialog(this, "An Error Occurred while Loading ".concat(files[i]), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TodoListApp app = new TodoListApp();
            app.setVisible(true); // Show the app
        });
    }
}