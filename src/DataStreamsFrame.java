import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataStreamsFrame extends JFrame
{
    private JTextArea originalTA;
    private JTextArea filteredTA;
    private JTextField searchTF;
    private JButton loadBtn;
    private JButton searchBtn;
    private JButton quitBtn;
    private JLabel fileLbl;

    private Path filePath = null;   // set once a file is loaded

    public DataStreamsFrame()
    {
        super("Data Streams - Lab 09");
        setLayout(new BorderLayout(5, 5));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // ---------- GUI panels ----------

    private JPanel createTopPanel()
    {
        JPanel topPnl = new JPanel(new GridLayout(2, 1));

        JPanel searchPnl = new JPanel();
        searchPnl.add(new JLabel("Search String:"));
        searchTF = new JTextField(30);
        searchTF.addActionListener(e -> searchFile());   // Enter key also searches
        searchPnl.add(searchTF);

        fileLbl = new JLabel("No file loaded", SwingConstants.CENTER);

        topPnl.add(searchPnl);
        topPnl.add(fileLbl);
        return topPnl;
    }

    private JPanel createDisplayPanel()
    {
        JPanel displayPnl = new JPanel(new GridLayout(1, 2, 10, 0));

        originalTA = new JTextArea();
        originalTA.setEditable(false);
        originalTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane leftScroll = new JScrollPane(originalTA);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Original File"));

        filteredTA = new JTextArea();
        filteredTA.setEditable(false);
        filteredTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane rightScroll = new JScrollPane(filteredTA);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Filtered Results"));

        displayPnl.add(leftScroll);
        displayPnl.add(rightScroll);
        return displayPnl;
    }

    private JPanel createButtonPanel()
    {
        JPanel btnPnl = new JPanel(new GridLayout(1, 3, 10, 0));

        loadBtn = new JButton("Load File");
        loadBtn.addActionListener(e -> loadFile());

        searchBtn = new JButton("Search File");
        searchBtn.setEnabled(false);   // only enabled after a file is loaded
        searchBtn.addActionListener(e -> searchFile());

        quitBtn = new JButton("Quit");
        quitBtn.addActionListener(e -> System.exit(0));

        btnPnl.add(loadBtn);
        btnPnl.add(searchBtn);
        btnPnl.add(quitBtn);
        return btnPnl;
    }

    // ---------- Actions ----------

    private void loadFile()
    {
        JFileChooser chooser = new JFileChooser();
        File workingDirectory = new File(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(workingDirectory);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            Path chosen = chooser.getSelectedFile().toPath();

            // Read the whole file with a Stream and show it on the left
            try (Stream<String> lines = Files.lines(chosen))
            {
                String text = lines.collect(Collectors.joining("\n"));
                originalTA.setText(text);
                originalTA.setCaretPosition(0);
                filteredTA.setText("");

                filePath = chosen;
                fileLbl.setText("Loaded: " + chosen.getFileName());
                searchBtn.setEnabled(true);
            }
            catch (IOException | UncheckedIOException ex)
            {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void searchFile()
    {
        if (filePath == null)
        {
            JOptionPane.showMessageDialog(this, "Please load a file first.");
            return;
        }

        String searchStr = searchTF.getText();
        if (searchStr.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please enter a search string.");
            return;
        }

        // Java Data Stream processing: filter the lines with a lambda expression
        try (Stream<String> lines = Files.lines(Paths.get(filePath.toString())))
        {
            String results = lines
                    .filter(line -> line.contains(searchStr))
                    .collect(Collectors.joining("\n"));

            if (results.isEmpty())
                filteredTA.setText("No lines contain \"" + searchStr + "\"");
            else
                filteredTA.setText(results);

            filteredTA.setCaretPosition(0);
        }
        catch (IOException | UncheckedIOException ex)
        {
            JOptionPane.showMessageDialog(this,
                    "Error reading file: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
