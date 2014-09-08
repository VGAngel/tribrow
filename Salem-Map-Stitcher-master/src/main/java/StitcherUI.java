import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class StitcherUI extends JPanel implements ActionListener, Logger {

	private JTextArea logArea;
	private JTextField directoryField;
	private JButton directoryButton;
	private JButton stitchButton;

	private FileManager fileManager;

	public StitcherUI() {
		super(new BorderLayout());
		setOpaque(true);

		createLogArea();

		fileManager = new FileManager(this);

		createStitchButton();
		createDirectoryPanel();
		directoryField.setText(fileManager.tryFindMapsDirectory());
	}

	private void createLogArea() {
		logArea = new JTextArea(5, 20);
		logArea.setMargin(new Insets(5, 5, 5, 5));
		logArea.setEditable(false);

		add(new JScrollPane(logArea), BorderLayout.CENTER);
	}

	private void createStitchButton() {
		stitchButton = new JButton("Stitch map");
		stitchButton.addActionListener(this);

		add(stitchButton, BorderLayout.SOUTH);
	}

	private void createDirectoryPanel() {
		directoryButton = new JButton("Select map directory");
		directoryButton.addActionListener(this);

		directoryField = new JTextField(20);
		directoryField.setMargin(new Insets(5, 5, 5, 5));
		directoryField.setEditable(false);

		JPanel directoryPanel = new JPanel();
		directoryPanel.add(directoryButton, BorderLayout.WEST);
		directoryPanel.add(directoryField, BorderLayout.EAST);

		add(directoryPanel, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == stitchButton) {
			if (fileManager.hasFoundMapDirectory()) {
				setButtonStatus(false);
				(new MapStitcherWorker(fileManager, this)).execute();
			} else {
				log("Error saving to the selected map directory.");
			}
		} else if (event.getSource() == directoryButton) {
			final JFileChooser fc = new JFileChooser();

			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fc.showOpenDialog(this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File mapDirectory = fc.getSelectedFile();
				fileManager.setMapDirectory(mapDirectory);
				directoryField.setText(mapDirectory.getAbsolutePath());
			}
		}
	}

	protected void setButtonStatus(boolean value) {
		stitchButton.setEnabled(value);
		directoryButton.setEnabled(value);
	}
	
	public void log(String line){
		logArea.append(line + "\n");
	}

	public static void createAndShowGUI() {
		JFrame frame = new JFrame("Salem Map Stitcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(new StitcherUI());

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
