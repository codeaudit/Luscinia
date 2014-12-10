package lusc.net.github.ui.db;
//
//  DatabaseView.java
//  Luscinia
//
//  Created by Robert Lachlan on 18/2/2008.
//  Copyright 2008 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import java.util.*;
import java.io.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import lusc.net.github.Defaults;
import lusc.net.github.Individual;
import lusc.net.github.Luscinia;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.AnalysisChoose;
import lusc.net.github.ui.IndividualEdit;
import lusc.net.github.ui.SaveSound;
import lusc.net.github.ui.SongInformation;
import lusc.net.github.ui.SpectrogramFileFilter;
import lusc.net.github.ui.TabType;
import lusc.net.github.ui.spectrogram.MainPanel;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class DatabaseView extends TabType implements ActionListener {
	
    private int newNodeSuffix = 1;
    private static String ADD_IND_COMMAND = "add individual";
	private static String ADD_SONG_COMMAND = "add song";
    private static String REMOVE_COMMAND = "remove";
    private static String INFORMATION_COMMAND = "information";
	private static String SONO_COMMAND = "sonogram";
	private static String ANALYZE_COMMAND = "analyze";
	private static String EXPAND_COMMAND="expand";
	private static String COPY_COMMAND="copy";
	private static String LOG_OUT_COMMAND = "logout";
	private static String MANAGE_USERS_COMMAND = "users";
	
	boolean collapsed=true;
   
	LinkedList tstore=new LinkedList();
	LinkedList ustore=new LinkedList();
	String query=null;
	private int [] indq={1,2};
	private int [] sonq={1,2,3};
	int addType=0;
	JButton manageUsers, addIndButton, addSongButton, removeButton, expandTreeButton, analysisButton, sonogramButton, expandButton, logOutButton, copyButton;
	JCheckBox informationCheckBox;
	
	File file;
	LinkedList spectrogramList=new LinkedList();
	LinkedList analysisList=new LinkedList();
	
	protected DatabaseTree treePanel;
	JPanel sidePanel, informationPanel;
	
	DataBaseController dbc;
	Defaults defaults;
	Luscinia luscinia;

    public DatabaseView(DataBaseController dbc, Defaults defaults, Luscinia luscinia) {
		
        //super(new BorderLayout());
        this.dbc=dbc;
		this.defaults=defaults;
		this.luscinia=luscinia;
		this.setLayout(new BorderLayout());
		isSdLogin=false;
        //Create the components.
        treePanel = new DatabaseTree(this, dbc.getDBName());
        populateTree(treePanel);
		treePanel.setPreferredSize(new Dimension(300, 600));
		informationPanel=new JPanel();
		//informationPanel.setPreferredSize(new Dimension(200, 600));
		buildButtonSideBar();
		
		this.setLayout(new BorderLayout());
		this.add(sidePanel, BorderLayout.LINE_START);
		this.add(treePanel, BorderLayout.CENTER);
		this.add(informationPanel, BorderLayout.LINE_END);
		
		//Updater updater=new Updater(dbc, defaults);
		//this.setPreferredSize(new Dimension(800, 600));
		//updateMeasurements();
	}
    
    public DataBaseController getDBController(){
    	return dbc;
    }
		
	public void buildButtonSideBar(){
        addIndButton = new JButton("Add Individual");
        addIndButton.setActionCommand(ADD_IND_COMMAND);
        addIndButton.addActionListener(this);
		addIndButton.setEnabled(true);
		
		addSongButton = new JButton("Add Song");
        addSongButton.setActionCommand(ADD_SONG_COMMAND);
        addSongButton.addActionListener(this);
		addSongButton.setEnabled(false);
        
        removeButton = new JButton("Remove");
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		
		informationCheckBox = new JCheckBox("Show Information");
		informationCheckBox.setSelected(true);
		informationCheckBox.setActionCommand(INFORMATION_COMMAND);
		informationCheckBox.addActionListener(this);
		
		sonogramButton = new JButton("Make Sonogram");
		sonogramButton.setActionCommand(SONO_COMMAND);
		sonogramButton.addActionListener(this);
		sonogramButton.setEnabled(false);
		
		analysisButton = new JButton("Analyze");
		analysisButton.setActionCommand(ANALYZE_COMMAND);
		analysisButton.addActionListener(this);
		
		copyButton = new JButton("Copy");
		copyButton.setActionCommand(COPY_COMMAND);
		copyButton.addActionListener(this);
		
		expandButton=new JButton("Expand Tree");
		expandButton.setActionCommand(EXPAND_COMMAND);
		expandButton.addActionListener(this);
		
		logOutButton=new JButton("Log-out");
		logOutButton.setActionCommand(LOG_OUT_COMMAND);
		logOutButton.addActionListener(this);
		
		manageUsers=new JButton("Manage Users");
		manageUsers.setActionCommand(MANAGE_USERS_COMMAND);
		manageUsers.addActionListener(this);
		
		sidePanel=new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		
		sidePanel.add(addIndButton);
		sidePanel.add(addSongButton);
		sidePanel.add(removeButton);
		sidePanel.add(informationCheckBox);
		sidePanel.add(sonogramButton);
		sidePanel.add(analysisButton);
		sidePanel.add(expandButton);
		sidePanel.add(copyButton);
		sidePanel.add(manageUsers);
		sidePanel.add(logOutButton);
    }
	
	public void clearUp(){
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			mp.cleanUp();
		}
		for (int i=0; i<analysisList.size(); i++){
			AnalysisChoose ac=(AnalysisChoose)analysisList.get(i);
			ac.cleanUp();
		}
	
	}
	
	
    public void populateTree(DatabaseTree treePanel) {
		
		extractFromDatabase();
		
		System.out.println(tstore.size()+" "+ustore.size());
		
		myNode nullpar=new myNode("temp");
		for (int i=0; i<tstore.size(); i++){
			String []nam=(String[])tstore.get(i);
			myNode chile=treePanel.addObject(null, nam[0], true);
			chile.dex=myIntV(nam[1]);
			
			//IndividualEdit ie=new IndividualEdit(treePanel, dbc, chile.dex, defaults);
			
			
			if (nam[0]=="Undetermined"){nullpar=chile;}
		}
		int numind=treePanel.rootNode.getChildCount();
		for (int i=0; i<ustore.size(); i++){
			String [] nam=(String[])ustore.get(i);
			if (nam[2]==null){nam[2]="-1";}
			int par=myIntV(nam[2]);
			boolean found=false;
			for (int j=0; j<numind; j++){
				myNode posspar=(myNode)treePanel.rootNode.getChildAt(j);
				if (posspar.dex==par){
					found=true;
					myNode chile=treePanel.addObject(posspar, nam[0], true);
					
					
					
					
					chile.dex=myIntV(nam[1]);
					j=numind;
				}
			}
			if (!found){
				myNode chile=treePanel.addObject(nullpar, nam[0], true);
				chile.dex=myIntV(nam[1]);
			}
			
		}
    }
	
	public int myIntV(String s){
		Integer p1=Integer.valueOf(s);
		int p=p1.intValue();
		return p;
	}
	
	
	public void extractFromDatabase(){
		tstore=null;
		tstore=new LinkedList();
		query="SELECT name, id FROM individual";
		tstore.addAll(dbc.readFromDataBase(query, indq));
		String[] s;
		String seg;
		int loc=0;
		for (int i=0; i<tstore.size(); i++){
			loc=i;
			s=(String[])tstore.get(i);
			seg=s[0];
			for (int j=i+1; j<tstore.size(); j++){
				s=(String[])tstore.get(j);
				if (seg.compareToIgnoreCase(s[0])<0){
					seg=s[0];
					loc=j;
				}
			}
			s=(String[])tstore.get(loc);
			tstore.remove(loc);
			tstore.add(0, s);
		}
		
		ustore=null;
		ustore=new LinkedList();
		query="SELECT name, id, IndividualID FROM songdata";
		ustore.addAll(dbc.readFromDataBase(query, sonq));
		
	}
	
	public void renameNode (myNode temp){
		if (temp.getLevel()>1){renameSong(temp);}
		else{renameIndividual(temp);}
		extractFromDatabase();
	}
	
	public void renameSong (myNode temp){
		String t=temp.toString();
		dbc.writeToDataBase("UPDATE songdata SET name='"+t+"' WHERE id="+temp.dex);
	}
	
	public void renameIndividual (myNode temp){
		String t=temp.toString();
		dbc.writeToDataBase("UPDATE individual SET name='"+t+"' WHERE id="+temp.dex);
	}
	
	public void removeFromDataBase(myNode temp){
		for (int j=0; j<temp.getChildCount(); j++){
			myNode temp2=(myNode)temp.getChildAt(j);
			removeSong(temp2);
		}
		if (temp.getLevel()>1){removeSong(temp);}
		else{removeIndividual(temp);}	
		extractFromDatabase();	
	}
	
	public void removeSong (myNode temp){
		System.out.println(spectrogramList.size());
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			if (temp.dex==mp.getSong().getSongID()){
				mp.cleanUp();
				mp=null;
			}
		}
		
		dbc.writeToDataBase("DELETE FROM songdata WHERE id="+temp.dex);
		dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+temp.dex);
		dbc.writeToDataBase("DELETE FROM element WHERE songID="+temp.dex);
	}
	
	
	public void removeIndividual(myNode temp){
		dbc.writeToDataBase("DELETE FROM individual WHERE id="+temp.dex);
		
		
		dbc.writeToDataBase("DELETE FROM songdata WHERE individualID="+temp.dex);
		//dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+temp.dex);
		//dbc.writeToDataBase("DELETE FROM element WHERE songID="+temp.dex);
		
		
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			if (temp.dex==mp.getSong().getIndividualID()){
				mp.cleanUp();
				mp=null;
			}
		}
		
	}
	
	public void addNewIndividual(myNode chile){
		String nam=chile.toString();
		dbc.writeToDataBase("INSERT INTO individual (name) VALUES ('"+nam+"')");
		extractFromDatabase();
		int maxind=-1;
		for (int i=0; i<tstore.size(); i++){
			String[]nam2=(String[])tstore.get(i);
			int p=myIntV(nam2[1]);
			if (p>maxind){maxind=p;}
		}
		chile.dex=maxind;
	}
	
	public void addNewSong(myNode chile, myNode par){
		String nam=chile.toString();
		dbc.writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+nam+"' , "+par.dex+")");
		extractFromDatabase();
		int maxind=-1;
		for (int i=0; i<ustore.size(); i++){
			String[]nam2=(String[])ustore.get(i);
			int p=myIntV(nam2[1]);
			if (p>maxind){maxind=p;}
		}
		chile.dex=maxind;
	}
	
	public void showInformationIndividual(){
		IndividualEdit i=new IndividualEdit(treePanel, dbc, treePanel.selnode[0].dex, defaults);
		informationPanel.removeAll();
		informationPanel.add(i);
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
		System.out.println("HERE1");
	}
	
	public void showInformationSong(){
		SongInformation si=new SongInformation(treePanel, dbc, treePanel.selnode[0].dex, defaults);
		informationPanel.removeAll();
		informationPanel.add(si);
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
		System.out.println("HERE2");
	}
	
	public void hideInformationPanel(){
		informationPanel.removeAll();
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
		System.out.println("HERE3");
	}
	
	    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try{
			if (ADD_SONG_COMMAND.equals(command)) {
				//int n = JOptionPane.showConfirmDialog(this,"Do you really want to add a wav file here?\n"+"(It will permanently remove whatever wav\n"+"is stored in the database)","Confirm Change", JOptionPane.YES_NO_OPTION);
				//if (n==0){openWav();}
				treePanel.addAbject();
			}
			else if (ADD_IND_COMMAND.equals(command)) {
				treePanel.addAbject();
			}
			else if (REMOVE_COMMAND.equals(command)) {
				int n = JOptionPane.showConfirmDialog(this,"Do you really want to permanently delete this?","Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (n==0){
					treePanel.removeCurrentNode();
					
				}
			}
			else if (INFORMATION_COMMAND.equals(command)){
				if (informationCheckBox.isSelected()){
					if (treePanel.selnode[0].getLevel()==1){
						showInformationIndividual();
					}
					else if (treePanel.selnode[0].getLevel()==2){
						showInformationSong();
					}
					else{
						hideInformationPanel();
					}
				}
				else{
					hideInformationPanel();
				}
			}
			else if (SONO_COMMAND.equals(command)) {
				openSpectrogram();
			}
			else if (ANALYZE_COMMAND.equals(command)) {
				AnalysisChoose ac=new AnalysisChoose(dbc, defaults);
				analysisList.add(ac);
			}
			else if (EXPAND_COMMAND.equals(command)) {
				if (collapsed){
					treePanel.expandNode();
					collapsed=false;
					expandButton.setText("Collapse tree");
				}
				else{
					treePanel.collapseNode();
					collapsed=true;
					expandButton.setText("Expand tree");
				}
			}
			else if (COPY_COMMAND.equals(command)) {copyDB();}
			else if (MANAGE_USERS_COMMAND.equals(command)){
				UserManagement um=new UserManagement(dbc);
				
			}
			else if (LOG_OUT_COMMAND.equals(command)) {
				//luscinia.removeConnection(this);
				luscinia.loggedOut(this);
			}
		}
		catch(Exception error){
			System.out.println(error);
			error.printStackTrace();
		}
    }
	/*
	public void openWav(){
		File [] file;
		dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+treePanel.selnode.dex);
		dbc.writeToDataBase("DELETE FROM element WHERE songID="+treePanel.selnode.dex);
		JFileChooser fc=new JFileChooser();
		String defPath=defaults.props.getProperty("path");
		if (defPath!=null){fc=new JFileChooser(defPath);}
		fc.addChoosableFileFilter(new SpectrogramFileFilter());
		//fc
		int returnVal = fc.showOpenDialog(DatabaseView.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file  = fc.getSelectedFiles();
			for (int i=0; i<file.length; i++){
				defPath= file[i].getPath();
				String defName=file[i].getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				if ((defName.endsWith(".wav"))||(defName.endsWith(".aiff"))||(defName.endsWith(".aif"))){
					System.out.println("HERE");
					treePanel.selnode.setUserObject(defName);
					renameSong(treePanel.selnode);
					dbc.writeSongIntoDatabase(defName, treePanel.selnode.dex, file[i]);
				}
			}
			file=null;
		}
	}
	 */
	/*
	public void backUpDB(){		
		int returnVal = JFileChooser.APPROVE_OPTION;
		JFileChooser fc=new JFileChooser();			
		String defPath=defaults.props.getProperty("path");
		if (defPath!=null){
			try{
				fc=new JFileChooser(defPath);
				File fs=new File(defPath, dbc.getDBaseName());
				fc.setSelectedFile(fs);
			}
			catch(Exception e){
				fc=new JFileChooser();
			}
		}
		else{
			fc=new JFileChooser();
		}
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			int cont=0;
			if (file.exists()){
				cont= JOptionPane.showConfirmDialog(this,"Do you really want to overwrite this file?\n"+"(It will be deleted permanently)","Confirm Overwrite", JOptionPane.YES_NO_OPTION);
			}
			if (cont==0){
				defPath=file.getPath();
				String defName=file.getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				BackUp bu=new BackUp(dbc, defPath, defaults);
			}
		}
	}
	*/
	public void copyDB(){
		SdLogin sdlogin=new SdLogin(this.luscinia, false, defaults);

		
		int n=JOptionPane.showOptionDialog(this, sdlogin, "Choose a location or database to copy to", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (n==0){
			DataBaseController dbc2=luscinia.getDBController();
			if (dbc2==null){
				JOptionPane.showMessageDialog(this,"Log-in to a second database before clicking ok!", "Copy error!", JOptionPane.OK_OPTION);
			}
			else{
				extractFromDatabase();
				System.out.println(tstore.size()+" "+ustore.size());
			
				int[][] idTranslater=new int[tstore.size()][2];
			
				for (int i=0; i<tstore.size(); i++){
					String []nam=(String[])tstore.get(i);
					int p=myIntV(nam[1]);
					idTranslater[i][0]=p;
					Individual individual=new Individual(dbc, p);
					individual.setDBC(dbc2);
					individual.insertIntoDB();
					idTranslater[i][1]=dbc2.readIndividualNameFromDB(individual.getName());
					System.out.println(idTranslater[i][0]+" "+idTranslater[i][1]);
				}
				
				
				
				int[][] songTranslater=new int[ustore.size()][2];
				
				for (int i=0; i<ustore.size(); i++){
					try{
						String []nam=(String[])ustore.get(i);
						int p=myIntV(nam[1]);
						Song song=dbc.loadSongFromDatabase(p, 0);
						
						songTranslater[i][0]=song.getSongID();
						songTranslater[i][1]=i+1;
						
						song.makeAudioFormat();
						int newid=-1;
						for (int j=0; j<idTranslater.length; j++){
							if (idTranslater[j][0]==song.getIndividualID()){
								newid=idTranslater[j][1];
								j=idTranslater.length;
							}
						}
						
						File f=File.createTempFile("ltmp", "wav");
						
						SaveSound ss=new SaveSound(song, song.getAf(), 0, song.getRDLength(), f); 
						dbc2.writeWholeSong(song, newid, f);
						
						
						f.delete();
					}
					catch(Exception e){
						dbc.reconnect();
					}
				}
				
				LinkedList schemes1=dbc.loadSchemes(true);
				LinkedList schemes2=dbc.loadSchemes(false);
				LinkedList so=new LinkedList();
				so.addAll(dbc2.readFromDataBase("SELECT name, id, IndividualID FROM songdata", sonq));
				
				
				for (int i=0; i<schemes1.size(); i++){
					
					LinkedList sch=(LinkedList)schemes1.get(i);

					String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
					String b=" , ";
					String t=")";
					String name=(String)sch.get(0);
					
					int be1=0;
					int bs1=0;
					
					for (int j=1; j<sch.size(); j++){
						int[] labels=(int[])sch.get(j);
						
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[0]){
								labels[0]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[1]){
								labels[1]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						
						String s2=s+"'"+name+"'"+b+labels[0]+b+labels[1]+b+1+b+be1+b+bs1+t;
						dbc2.writeToDataBase(s2);
					}
				}
				
				
				for (int i=0; i<schemes2.size(); i++){
					
					LinkedList sch=(LinkedList)schemes2.get(i);
					
					String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
					String b=" , ";
					String t=")";
					String name=(String)sch.get(0);
					
					int be1=0;
					int bs1=0;
					
					int def=-1;
					
					for (int j=1; j<sch.size(); j++){
						int[] labels=(int[])sch.get(j);
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[0]){
								labels[0]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						String s2=s+"'"+name+"'"+b+labels[0]+b+def+b+1+b+be1+b+bs1+t;
						dbc2.writeToDataBase(s2);
					}
				}
				
				
				
				
				
				
				
				
				luscinia.loggedOut();
			}
		}
	}
	
	public void openWav(myNode parentNode, Object child){
		File [] file;
		JFileChooser fc=new JFileChooser();
		String defPath=defaults.props.getProperty("path");
		
		if (defPath!=null){fc=new JFileChooser(defPath);}
		
		SpectrogramFileFilter[] sff={new SpectrogramFileFilter("wav"), new SpectrogramFileFilter("aiff"), new SpectrogramFileFilter("aif"), new SpectrogramFileFilter("mp3")};
		
		fc.addChoosableFileFilter(sff[0]);
		fc.addChoosableFileFilter(sff[1]);
		fc.addChoosableFileFilter(sff[2]);
		fc.addChoosableFileFilter(sff[3]);
		int p=defaults.getDefaultSoundFormat();
		
		fc.setFileFilter(sff[p]);
		
		fc.setMultiSelectionEnabled(true);
		int returnVal = fc.showOpenDialog(DatabaseView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file  = fc.getSelectedFiles();
			treePanel.selnode=new myNode[file.length];
			for (int i=0; i<file.length; i++){
				defPath=file[i].getParent();
				String defName=file[i].getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				SpectrogramFileFilter sfu=(SpectrogramFileFilter)fc.getFileFilter();
				
				if (sfu==sff[0]){
					defaults.setDefaultSoundFormat(0);
				}
				else if (sfu==sff[1]){
					defaults.setDefaultSoundFormat(1);
				}
				else if (sfu==sff[2]){
					defaults.setDefaultSoundFormat(2);
				}
				
				String lcdefn=defName.toLowerCase();
				
				if ((lcdefn.endsWith(".wav"))||(lcdefn.endsWith(".aiff"))||(lcdefn.endsWith(".aif"))||(lcdefn.endsWith(".mp3"))){
					myNode ch=treePanel.addObject(parentNode, child, true);
					addNewSong(ch, parentNode);
					treePanel.selnode[i]=ch;				
					treePanel.selnode[i].setUserObject(defName);
					renameSong(treePanel.selnode[i]);
					dbc.writeSongIntoDatabase(defName, treePanel.selnode[i].dex, file[i]);
				}
			}
			file=null;
		}
	}

	
	public void openSpectrogram(){
		if (treePanel.selnode!=null){
			for (int i=0; i<treePanel.selnode.length; i++){
				MainPanel mp=new MainPanel(dbc, treePanel.selnode[i].dex, defaults, spectrogramList);
				if (!mp.getSong().getLoaded()){
					//openWav();
					//if the song is not present in the database, then some error message should present itself here
				}
				else{mp.startDrawing();}
		
				spectrogramList.add(mp);
			}
		}
		
	}
	
	public void updateMeasurements(){
		
		
		System.out.println("UPDATING: "+ustore.size());
		
		SpectrPane s;
		
		int a=ustore.size();
		int b=a*3/10;
		
		for (int i=0; i<b; i++){
			String [] nam=(String[])ustore.get(i);
			int p=myIntV(nam[1]);
			System.out.println("a");
			
			Song song=dbc.loadSongFromDatabase(p, 0);
			if (song.getMaxF()<=0){
				defaults.getSongParameters(song);
			}
			
			//ALERT ARE THE FOLLOWING LINES RIGHT? I SHOULDN'T BE SETTING DEFAULTS HERE!
			song.setDynRange(40);
			song.setEchoRange(50);
			song.setEchoComp(1.0f);
			song.setDynEqual(200);
			
			song.setFFTParameters();
			System.out.println("b");
			s=new SpectrPane(song, false, false, null);
			System.out.println("c");
			
			//s.restart();
			System.out.println("d");
			
			song.setFFTParameters2(song.getNx());
			song.makeMyFFT(0, song.getNx());
			s.setNout(song.getOut());
			//s.relocate(0, song.nx);
			
			System.out.println("updating: "+song.getName());
			
			song.getMeasurer().updateTrillMeasures();
			dbc.writeSongMeasurements(song);
			
			
			song.clearUp();
			song=null;
			s.clearUp();
			s=null;
			
			System.gc();

			
			
		}
	}
	
	
	
	public void cleanUp(){
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			mp.cleanUp();
			mp=null;
		}
	}
	
}