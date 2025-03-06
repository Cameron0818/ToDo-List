import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;  // Import the File class

public class TodoListApp extends JFrame {
    private DefaultListModel<String> listModel;
    private JList<String> taskList;
    private JTextField taskInput;

    public TodoListApp() {
        setupFrame();      // Configure the JFrame
        setupComponents(); // Add UI components
        loadTasks();       // Load saved tasks
    }

    private void setupFrame() {
        setTitle("Todo List");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.BLACK);
    }

    private void setupComponents() {
        // Task input field and buttons
        taskInput = new JTextField();
        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");

        // Task list setup
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(taskList);

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
        scrollPane.setBackground(Color.BLACK);

        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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
        String task = taskInput.getText();

        if (task.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        listModel.addElement(task);
        taskInput.setText("");

    }   

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "No Task Selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        listModel.removeElementAt(selectedIndex);
    }

    private void saveTasks() {
        File tasksFile = new File("C:\\Users\\Camer\\OneDrive\\Desktop\\To-Do List\\src\\savedTasks.txt");
        try {
            if (!tasksFile.exists()) {
                tasksFile.createNewFile(); // Creates the file if it doesn't exist
            }
        
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "There was an Error with the File Path", "Error", JOptionPane.ERROR_MESSAGE);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tasksFile))) {
        // Iterate over the tasks in the listModel and write them to the file
        for (int i = 0; i < listModel.size(); i++) {
            String task = listModel.getElementAt(i); // Get each task from the list model
            writer.write(task); // Write task to the file
            writer.newLine(); // Add a new line after each task
        }
        }   catch (IOException e) {
        // Handle any exceptions
        JOptionPane.showMessageDialog(this, "An Error Occurred while Saving the Tasks", "Error", JOptionPane.ERROR_MESSAGE);
        
        }
    }    

    private void loadTasks() {
        File tasksFile = new File("C:\\Users\\Camer\\OneDrive\\Desktop\\To-Do List\\src\\savedTasks.txt");

        if (tasksFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(tasksFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    listModel.addElement(line);
                }
            }catch (IOException e) {
                // Handle any exceptions
                JOptionPane.showMessageDialog(this, "An Error Occurred while Loading the Tasks", "Error", JOptionPane.ERROR_MESSAGE);
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