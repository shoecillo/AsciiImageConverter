package com.sh.converter.visual;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.sh.converter.AsciiImageConverter;
import com.sh.converter.PathsEnum;

public class WinAsciiConverter extends Shell implements Observer  {
	private Button btnBrowse;
	private Label lblFileImage;
	private Button btnConvert;
	private List lsHtmls;
	private ProgressBar progressBar;
	private Display display;
	private Label lblPerc;
	
	private java.util.List<File> myFiles;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			WinAsciiConverter shell = new WinAsciiConverter(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public WinAsciiConverter(Display display) {
		super(display, SWT.SHELL_TRIM);
		this.display = display;
		setLayout(new GridLayout(1, false));
		
		Group grpChooseImage = new Group(this, SWT.NONE);
		grpChooseImage.setText("Choose Image");
		grpChooseImage.setLayout(new GridLayout(3, false));
		GridData gd_grpChooseImage = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_grpChooseImage.heightHint = 57;
		grpChooseImage.setLayoutData(gd_grpChooseImage);
		new Label(grpChooseImage, SWT.NONE);
		new Label(grpChooseImage, SWT.NONE);
		new Label(grpChooseImage, SWT.NONE);
		
		btnBrowse = new Button(grpChooseImage, SWT.NONE);
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent evt) {
				browseFile();
			}
		});
		btnBrowse.setText("Browse...");
		
		lblFileImage = new Label(grpChooseImage, SWT.NONE);
		GridData gd_lblFileImage = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_lblFileImage.widthHint = 555;
		lblFileImage.setLayoutData(gd_lblFileImage);
		
		btnConvert = new Button(grpChooseImage, SWT.NONE);
		btnConvert.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent evt) {
				convertImage();
			}
		});
		btnConvert.setText("Convert");
		
		Group grOpps = new Group(this, SWT.NONE);
		grOpps.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_grOpps = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2);
		gd_grOpps.heightHint = 30;
		grOpps.setLayoutData(gd_grOpps);
		
		lsHtmls = new List(grOpps, SWT.BORDER);
		lsHtmls.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent evt) {
				openFile();
			}
		});
		
		Group group = new Group(this, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_group.heightHint = 31;
		group.setLayoutData(gd_group);
		
		progressBar = new ProgressBar(group, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		lblPerc = new Label(group, SWT.NONE);
		GridData gd_lblPerc = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblPerc.widthHint = 45;
		lblPerc.setLayoutData(gd_lblPerc);
		lblPerc.setText("0%");
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("AsciiConverter");
		setSize(752, 372);
		btnConvert.setEnabled(false);
		
		File f = new File(PathsEnum.GENPATH.getValue());
		if(!f.exists()) { f.mkdir(); }
		fillList();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private void browseFile() {
		
		FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
		String[] ext = {"*.png","*.jpg","*.gif"};
		dlg.setFilterExtensions(ext);
		String resp = dlg.open();
		if(resp != null) {
			lblFileImage.setText(resp);
			btnConvert.setEnabled(true);
		}
		
	}
	
	private void convertImage() {
		
		String path = lblFileImage.getText();
		AsciiImageConverter conv = new AsciiImageConverter(path, this);
		lblPerc.setText("0%");
		display.asyncExec(conv);
		
	}
	
	private void fillList() {
		lsHtmls.removeAll();
		File f = new File(PathsEnum.GENPATH.getValue());
		File[] htmls = f.listFiles();
		myFiles = Arrays.stream(htmls).collect(Collectors.toList());
		myFiles.stream().forEach(it -> { lsHtmls.add(it.getName());});
	}
	
	private void openFile() {
		if(lsHtmls.getSelectionIndex() != -1) {
			
			File f = myFiles.get(lsHtmls.getSelectionIndex());
			try {
				Desktop.getDesktop().open(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public void update(Observable o, Object counter) {
		Integer resp = (Integer) counter;
		if(resp.intValue() == -1) {
			fillList();
			
		}else {
			progressBar.setSelection(resp.intValue());
			lblPerc.setText(MessageFormat.format("{0}%", resp.intValue()));
		}
		
	}
	
	
}
