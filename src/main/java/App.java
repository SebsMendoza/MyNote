import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;

public class App extends JFrame {
    private JPanel panelPrincipal;
    protected JTextArea textArea;
    private JScrollPane scrollTextArea;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuEdit;
    private JMenu menuHelp;
    private JMenuItem itemNew;
    private JMenuItem itemOpen;
    private JMenuItem itemSave;
    private JMenuItem itemSaveAs;
    private JMenuItem itemPrint;
    private JMenuItem itemUndo;
    private JMenuItem itemRedo;
    private JMenuItem itemCopy;
    private JMenuItem itemCut;
    private JMenuItem itemPaste;
    private JMenuItem itemAbout;
    private JMenuItem itemGoHelp;
    private JTextArea textArea2;
    private JScrollPane scrollTextArea2;
    private UndoManager unDo;
    private String name;

    public App() {
        initComponent();
    }

    private void initComponent() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 500));

        panelPrincipal = new JPanel();
        textArea = new JTextArea();
        textArea2 = new JTextArea();
        scrollTextArea = new JScrollPane();
        scrollTextArea2 = new JScrollPane();
        panelPrincipal.setLayout(new BorderLayout());

        scrollTextArea.setViewportView(textArea);
        panelPrincipal.add(scrollTextArea, BorderLayout.CENTER);
        textArea.setLineWrap(true);
        textArea.setTabSize(4);

        //Segunda área de texto donde se vería la ejecución del cmd
//        scrollTextArea2.setViewportView(textArea2);
//        panelPrincipal.add(scrollTextArea2, BorderLayout.SOUTH);
//        textArea2.setLineWrap(true);
        //textArea2.setSize();

        //Creación de objeto UndoManager y establecemos el tamaño del buffer de operaciones
        unDo = new UndoManager();
        unDo.setLimit(10);
        //Añadimos un escuchador para eventos de tipo UndoableEdit, y guardo en el objeto UndoManager la acción a
        // deshacer
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                unDo.addEdit(e.getEdit());
            }
        });

        setTitle("My IDE");

        //Barra de herramientas
        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        menuEdit = new JMenu("Edit");
        menuHelp = new JMenu("Help");

        //Items del boton File
        itemNew = new JMenuItem("New");
        itemOpen = new JMenuItem("Open");
        itemSave = new JMenuItem("Save");
        itemSaveAs = new JMenuItem("Save as...");
        itemPrint = new JMenuItem("Print");
        menuFile.add(itemNew);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.add(itemSaveAs);
        menuFile.add(itemPrint);
        menuBar.add(menuFile);

        //Items del boton Edit
        itemUndo = new JMenuItem("Undo");
        itemRedo = new JMenuItem("Redo");
        itemCopy = new JMenuItem("Copy");
        itemCut = new JMenuItem("Cut");
        itemPaste = new JMenuItem("Paste");
        menuEdit.add(itemUndo);
        menuEdit.add(itemRedo);
        menuEdit.add(itemCopy);
        menuEdit.add(itemCut);
        menuEdit.add(itemPaste);
        menuBar.add(menuEdit);

        //Items del boton Help
        itemAbout = new JMenuItem("About");
        itemGoHelp = new JMenuItem("Go to help");
        menuHelp.add(itemAbout);
        menuHelp.add(itemGoHelp);
        menuBar.add(menuHelp);

        //Escuchadores de los botones
        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("java files", ".java");
                    chooser.setFileFilter(filter);

                    File selectedFile;

                    BufferedReader in;
                    FileReader reader = null;
                    if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
                        selectedFile = chooser.getSelectedFile();
                        if (selectedFile.canRead() && selectedFile.exists()) {
                            textArea.setText("");
                            reader = new FileReader(selectedFile);
                        }
                    }
                    in = new BufferedReader(reader);
                    String inputLine = in.readLine();
                    while (inputLine != null) {
                        textArea.append(inputLine + "\n");
                        inputLine = in.readLine();
                    }
                    in.close();
                } catch (IOException | NullPointerException ignored) {
                }
            }
        });

        itemSaveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileC = new JFileChooser();
                int ret = fileC.showSaveDialog(fileC);

                if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
                    java.io.File sFile = fileC.getSelectedFile();

                    try (FileWriter writer = new FileWriter(sFile)) {
                        textArea.write(writer);
                    } catch (IOException ignored) {

                    }
                }
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (name == null) {
                    JFileChooser fc = new JFileChooser();
                    if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION)
                        name = fc.getSelectedFile().getAbsolutePath();
                }
                if (name != null) {
                    Formatter out = null;
                    try {
                        out = new Formatter(name);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    String filename = name;
                    out.format("%s", textArea.getText());
                    out.close();
                }
            }
        });

        itemPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textArea.print();
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                }
            }
        });

        itemNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });

        itemCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (textArea.getSelectedText() != null) {
                    StringSelection selection = new StringSelection("" + textArea.getSelectedText());
                    clipboard.setContents(selection, selection);
                }
            }
        });

        itemPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(null);
                if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        textArea.replaceSelection("" + data.getTransferData(DataFlavor.stringFlavor));
                    } catch (IOException | UnsupportedFlavorException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        itemCut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.cut();
            }
        });

        itemUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (unDo.canUndo()) unDo.undo();
            }
        });

        itemRedo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (unDo.canRedo()) unDo.redo();
            }
        });

        itemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Welcome to MyIDE\nDeveloped by Sebastian Mendoza");
            }
        });

        itemGoHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Desktop link = Desktop.getDesktop();
                try {
                    link.browse(new URI("https://dev.java/"));
                } catch (URISyntaxException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.setJMenuBar(menuBar);
        add(panelPrincipal);
        pack();
    }
}
