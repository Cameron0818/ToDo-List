import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * TodoListApp - A Swing-based application combining a calendar view with task management features.
 * Allows users to:
 * - Navigate months and weeks
 * - Add/delete tasks for specific dates
 * - Persist tasks to date-based text files
 */
public class TodoListApp extends JFrame {
    // Maps dates to their respective task list models (cached for performance)
    private final Map<LocalDate, DefaultListModel<String>> dateModels = new HashMap<>();
    
    // Current calendar view states
    private LocalDate currentWeekStart;  // Starting Monday of the visible week
    private YearMonth currentMonth;      // Currently displayed month in calendar
    
    // UI components
    private JLabel monthLabel;          // Displays current month/year
    private JPanel calendarPanel;        // Contains month navigation and day grid
    private JTabbedPane tabbedPane;      // Displays week days with task lists
    private JTextField taskInput;        // Input field for new tasks

    /**
     * Constructor initializes application state and UI
     */
    public TodoListApp() {
        // Initialize calendar states to current date
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        currentMonth = YearMonth.now();
        
        setupFrame();        // Configure main window properties
        setupComponents();   // Build UI components
        updateTabsForWeek(currentWeekStart);  // Load initial week view
    }

    /**
     * Configures main window properties
     */
    private void setupFrame() {
        setTitle("Todo List with Calendar");
        setSize(870, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center window
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
    }

    /**
     * Builds calendar panel with month navigation and day grid
     */
    private void setupCalendarPanel() {
        calendarPanel = new JPanel(new BorderLayout());
        
        // Month navigation panel
        JPanel navigationPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel();
        
        navigationPanel.add(prevButton);
        navigationPanel.add(monthLabel);
        navigationPanel.add(nextButton);

        // Day grid panel (7 columns for days of week)
        JPanel daysPanel = new JPanel(new GridLayout(0, 7));
        calendarPanel.add(navigationPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);

        // Month navigation handlers
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        updateCalendar();  // Initial calendar render
        add(calendarPanel, BorderLayout.NORTH);
    }

    /**
     * Updates calendar display for current month
     */
    private void updateCalendar() {
        // Update month/year header
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) 
            + " " + currentMonth.getYear());

        JPanel daysPanel = (JPanel) calendarPanel.getComponent(1);
        daysPanel.removeAll();  // Clear previous days

        // Add day headers (Monday-Sunday)
        for (DayOfWeek day : DayOfWeek.values()) {
            JLabel label = new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER);
            daysPanel.add(label);
        }

        // Calculate date range to display (including overlapping month days)
        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate firstDisplayed = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastOfMonth = currentMonth.atEndOfMonth();
        LocalDate lastDisplayed = lastOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Create day buttons for visible calendar range
        for (LocalDate date = firstDisplayed; !date.isAfter(lastDisplayed); date = date.plusDays(1)) {
            final LocalDate currentDate = date;  // For use in lambda
            JButton dayButton = new JButton(String.valueOf(currentDate.getDayOfMonth()));
            
            // Disable buttons from adjacent months
            dayButton.setEnabled(currentDate.getMonth() == currentMonth.getMonth());
            
            dayButton.addActionListener(e -> selectDate(currentDate));
            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    /**
     * Handles date selection from calendar
     * @param date Selected date
     */
    private void selectDate(LocalDate date) {
        // Adjust week view to contain selected date
        currentWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        updateTabsForWeek(currentWeekStart);
    }

    /**
     * Updates week view tabs with tasks for each day
     * @param startOfWeek Starting Monday of the week to display
     */
    private void updateTabsForWeek(LocalDate startOfWeek) {
        tabbedPane.removeAll();  // Clear previous tabs

        // Create tab for each day in week
        for (int i = 0; i < 7; i++) {
            final LocalDate date = startOfWeek.plusDays(i);
            // Format tab title (e.g., "Monday (Jan 1)")
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            String tabTitle = String.format("%s (%s)", dayName, date.format(DateTimeFormatter.ofPattern("MMM d")));

            // Get or create task list model for date
            DefaultListModel<String> model = dateModels.computeIfAbsent(date, k -> {
                DefaultListModel<String> m = new DefaultListModel<>();
                loadTasksForDate(date, m);  // Load from file if exists
                return m;
            });

            // Create task list with dark theme
            JList<String> list = new JList<>(model);
            list.setBackground(Color.BLACK);
            list.setForeground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(list);
            tabbedPane.addTab(tabTitle, scrollPane);

            // Enable keyboard deletion
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        deleteTasks();
                    }
                }
            });
        }

        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    /**
     * Loads tasks from date-based text file
     * @param date Date to load tasks for
     * @param model List model to populate with tasks
     */
    private void loadTasksForDate(LocalDate date, DefaultListModel<String> model) {
        String fileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt";
        File taskFile = new File(fileName);

        if (taskFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(taskFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    model.addElement(line);
                }
            } catch (IOException e) {
                // Silent handling for missing files
            }
        }
    }

    /**
     * Initializes main UI components
     */
    private void setupComponents() {
        setupCalendarPanel();

        // Task input components
        taskInput = new JTextField(34);
        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");

        // Size customization
        taskInput.setPreferredSize(new Dimension(200, 24));
        addButton.setPreferredSize(new Dimension(110, 30));
        deleteButton.setPreferredSize(new Dimension(110, 30));

        // Styling
        addButton.setBorder(new LineBorder(Color.BLACK, 3, true));
        deleteButton.setBorder(new LineBorder(Color.BLACK, 3, true));

        tabbedPane = new JTabbedPane();

        // Button panel layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton, BorderLayout.WEST);
        buttonPanel.add(taskInput, BorderLayout.CENTER);
        buttonPanel.add(deleteButton, BorderLayout.EAST);

        // Color coding
        addButton.setBackground(new Color(34, 139, 34));  // Forest green
        addButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(139, 0, 0));  // Dark red
        deleteButton.setForeground(Color.WHITE);

        // Add main components to frame
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTasks());
        taskInput.addActionListener(e -> addTask());

        // Auto-save on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });
    }

    /**
     * Adds new task to current day's list
     */
    private void addTask() {
        String task = taskInput.getText().trim();
        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get selected day's model
        int selectedTab = tabbedPane.getSelectedIndex();
        LocalDate date = currentWeekStart.plusDays(selectedTab);
        DefaultListModel<String> model = dateModels.get(date);
        
        model.addElement(task);
        taskInput.setText("");  // Clear input
    }

    /**
     * Deletes selected tasks from current day's list
     */
    @SuppressWarnings("unchecked")
    private void deleteTasks() {
        int selectedTab = tabbedPane.getSelectedIndex();
        LocalDate date = currentWeekStart.plusDays(selectedTab);
        DefaultListModel<String> model = dateModels.get(date);
        
        // Get selected items from UI component
        JList<String> list = (JList<String>) ((JScrollPane) tabbedPane.getComponentAt(selectedTab)).getViewport().getView();
        int[] selectedIndices = list.getSelectedIndices();

        // Remove from highest index first to prevent shifting
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            model.remove(selectedIndices[i]);
        }
    }

    /**
     * Persists all tasks to date-based text files
     */
    private void saveTasks() {
        for (Map.Entry<LocalDate, DefaultListModel<String>> entry : dateModels.entrySet()) {
            LocalDate date = entry.getKey();
            DefaultListModel<String> model = entry.getValue();
            String fileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (int i = 0; i < model.size(); i++) {
                    writer.write(model.getElementAt(i));
                    writer.newLine();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tasks for " + date, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Entry point: Launches application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TodoListApp app = new TodoListApp();
            app.setVisible(true);
        });
    }
}