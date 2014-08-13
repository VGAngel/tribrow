package de.d2dev.fourseasons.swing;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import com.google.common.base.Preconditions;

/**
 * log4j appender using a {@link JTextArea}.
 * @author Sebastian Bordt
 *
 */
public class JTextAreaAppender extends WriterAppender {
	
	private JTextArea textArea = null;

	public JTextAreaAppender(JTextArea textArea) {
		this.layout = new SimpleLayout();
		this.textArea = Preconditions.checkNotNull( textArea );
	}
	
	@Override
	public void append(LoggingEvent event) {
		final String message = this.layout.format(event);
		
		// append message to textarea in swing thread
		SwingUtilities.invokeLater( new Runnable() {
			
			@Override
			public void run() {
				JTextAreaAppender.this.textArea.append( message );
			}
		});
	}

}
