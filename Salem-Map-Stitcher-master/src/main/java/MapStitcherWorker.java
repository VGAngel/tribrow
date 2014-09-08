import java.util.List;

import javax.swing.SwingWorker;


public class MapStitcherWorker extends SwingWorker<String, String> implements Logger{
	
	private MapStitcher mapStitcher;
	private FileManager fileManager;
	private StitcherUI ui;
	
	public MapStitcherWorker(FileManager fileManager, StitcherUI ui){
		this.fileManager = fileManager;
		this.mapStitcher = new MapStitcher(this);
		this.ui = ui;
	}

	@Override
	protected String doInBackground() throws Exception {
		ui.log("Starting stitching...");
		mapStitcher.stitchMaps(fileManager.getMapDirectory());
		return null;
	}
	
	public void log(String s){
		publish(s);
	}

	protected void process(List<String> logsSoFar) {
		for (String line : logsSoFar) {
			ui.log(line);
		}
	}

	protected void done() {
		ui.log("Finished stitching...");
		ui.setButtonStatus(true);
	}
}
