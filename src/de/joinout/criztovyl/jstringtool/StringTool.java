package de.joinout.criztovyl.jstringtool;

import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.joinout.criztovyl.tools.gui.BetterGUI;

public class StringTool extends BetterGUI {

	private StyledText stInput;
	private Text tPattern, tResults, tDisplayAs;
	private Button cbQuotePattern, cbCustomDisplay;
	private Label lUnicodeCharClassInfo;
	private Logger logger;

	public StringTool(){
		
		logger = LogManager.getLogger();

		stInput = new StyledText(this.getShell(), SWT.MULTI |  SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		
		tPattern = new Text(getShell(), SWT.SINGLE |SWT.BORDER);
		tResults = new Text(getShell(), SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		tDisplayAs = new Text(getShell(), SWT.SINGLE | SWT.BORDER);
		
		cbQuotePattern = new Button(getShell(), SWT.CHECK);
		cbCustomDisplay = new Button(getShell(), SWT.CHECK);
		
		lUnicodeCharClassInfo = new Label(getShell(), SWT.WRAP);
		
		cbQuotePattern.setText("Quote search string");
		cbCustomDisplay.setText("Custom display:");
		
		stInput.setText(String.format("This order was placed for QT3000! OK?%n"
				+ "This present was placed for fhgt! OK?%n"
				+ "This boo	 was placed for trulla! OK?%n"
				+ "This foo was placed for troet! OK?%n"
				+ "This order was placed for chschulz96! OK?%n"
				+ "This order was placed for peter! OK?%n"));
		
		tPattern.setText("\\w+ (\\w+).+ (.+)!");
		
		lUnicodeCharClassInfo.setText(String.format("\\w doesn't match special chars like ß?%n\tInclude (?U) in your pattern."));

		Listener changeListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(logger.isDebugEnabled()) logger.debug("A widget listening to changed. (Updates results)");
				search();
			}
		};
		
		SelectionAdapter cbSelection = new SelectionAdapter() {
			
			@Override
		    public void widgetSelected(SelectionEvent e)
		    {
				if(logger.isDebugEnabled()) logger.debug("A widget listening to was clicked. (Updates results)");
		        search();
		    }
		};

		stInput.addListener(SWT.CHANGED, changeListener);
		tPattern.addListener(SWT.CHANGED, changeListener);
		cbQuotePattern.addSelectionListener(cbSelection);
		cbCustomDisplay.addSelectionListener(cbSelection);
		tDisplayAs.addListener(SWT.CHANGED, changeListener);


		add(stInput);
		resize(new Point(600, 600));
		right(tPattern);
		right(cbCustomDisplay);
		right(tDisplayAs);
		below(tResults, tPattern);
		resize(new Point(400, 300));
		below(cbQuotePattern);
		below(lUnicodeCharClassInfo);
		
		getShell().setMinimumSize(1000, 600);

	}

	private void search(){
		if(tPattern.getText().equals("")) return;
		try {
			Pattern p = Pattern.compile(cbQuotePattern.getSelection() ? Pattern.quote(tPattern.getText()) : tPattern.getText());
			Matcher m = p.matcher(stInput.getText());

			stInput.setStyleRange(null);
			tResults.setText("");

			while (m.find()){
				
				StyleRange sr = new StyleRange();
				String str = cbCustomDisplay.getSelection() ? tDisplayAs.getText() : "$1 $2 $3 $4";
				
				sr.start = m.start();
				sr.length = m.end() - m.start();
				sr.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
				stInput.setStyleRange(sr);
				
				for(int i = 1; i <= m.groupCount(); i++) str = str.replaceAll("\\$" + i + "(?=\\D|$)", m.group(i));
				
				try{
					str = String.format(str);
				} catch(IllegalFormatException e){
					if(logger.isDebugEnabled()) logger.debug("Given RegEx isn't valid yet. Happens i.e. when changing the pattern and it has unclosed a brackets.");
				}
				
				tResults.append(String.format("%s%n", str));
				if(logger.isDebugEnabled()) logger.debug("Result string: {}", str);
				if(logger.isDebugEnabled()) logger.debug("Match finding loop.");
			}
			
			tPattern.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));

		} catch(PatternSyntaxException e){
			tPattern.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		} catch(IllegalStateException e){
			if(logger.isWarnEnabled()) logger.warn(e.getMessage());
		}
	}
	
	public void start(){
		search();
		super.start();
	}
}
