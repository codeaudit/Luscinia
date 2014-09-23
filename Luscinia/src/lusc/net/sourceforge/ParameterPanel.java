package lusc.net.sourceforge;
//
//  ParameterPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/10/07.
//  Copyright 2007 R.F.Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ParameterPanel extends JPanel{


	/**
	 * 
	 */
	private static final long serialVersionUID = 4656703390547940972L;

	JRadioButton[][] buttonArray=new JRadioButton[8][17];
	
	String[] parameters={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak freq. change", "Mean freq. change",
						"Median freq. change", "Fund. freq. change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato amplitude", "Vibrato rate", 
						"Time", "Gap between elements"};
						
	String[] headings={"Parameter", "Mean", "Max", "Min", "Time Max", "Time Min", "Start", "End", "Standard Deviation"};
	
	JLabel byRowLabel=new JLabel("Normalize per row");
	
	JRadioButton byRow=new JRadioButton();
	
	
	SongGroup sg;
	LinkedList compScheme;
	JPanel cpane;
	int mode=0;
	
	public  ParameterPanel (SongGroup sg){
		this.sg=sg;
		mode=1;
		parameterSetting();
		makeFrame();
	}
	
	public ParameterPanel (SongGroup sg, LinkedList compScheme){
		this.sg=sg;
		this.compScheme=compScheme;
		mode=0;
		parameterSetting();
		makeFrame();
	}
	
	public void parameterSetting(){
	
		JPanel buttonPanel=new JPanel(new GridLayout(17, 9));
		
		for (int i=0; i<9; i++){
			JLabel label=new JLabel(headings[i]);
			buttonPanel.add(label);
		}
		
		for (int i=0; i<14; i++){
		
			JLabel label=new JLabel(parameters[i]);
			buttonPanel.add(label);
			
			for (int j=0; j<8; j++){
				buttonArray[j][i]=new JRadioButton(" ");
				buttonPanel.add(buttonArray[j][i]);
			}
		}	
		
		for (int i=0; i<2; i++){
			JLabel label=new JLabel(parameters[i+14]);
			buttonPanel.add(label);
			
			buttonArray[0][i+14]=new JRadioButton(" ");
			buttonPanel.add(buttonArray[0][i+14]);
			
			for (int j=0; j<7; j++){
				JLabel blank=new JLabel(" ");
				buttonPanel.add(blank);
			}
		}
		
	
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Parameters to compare"));
		
		cpane=new JPanel();
		cpane.setLayout(new BorderLayout());
		cpane.add(buttonPanel, BorderLayout.LINE_START);
		
		JPanel optionsPanel=new JPanel(new BorderLayout());
		optionsPanel.add(byRowLabel, BorderLayout.LINE_START);
		optionsPanel.add(byRow, BorderLayout.CENTER);
		
		cpane.add(optionsPanel, BorderLayout.SOUTH);
	}
		
	public void makeFrame(){
		JPanel contentpane=new JPanel();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		contentpane.add(cpane, BorderLayout.LINE_START);
		
		JPanel bottomPane=new JPanel(new BorderLayout());
		contentpane.add(bottomPane, BorderLayout.SOUTH);
		
		this.add(contentpane);
	}
	
	public void startAnalysis(){
	
		sg.makeNames();
	
		boolean[][] parameterMatrix=new boolean[8][17];
		
		for (int i=0; i<8; i++){
			for (int j=0; j<17; j++){
				if ((buttonArray[i][j]!=null)&&(buttonArray[i][j].isSelected())){
					parameterMatrix[i][j]=true;
				}
				else{
					parameterMatrix[i][j]=false;
				}
				System.out.print(parameterMatrix[i][j]+" ");
			}
			System.out.println();
		}
		sg.calculateSummaries(parameterMatrix);
		sg.scoresEle=sg.calculateDistancesFromParameters(parameterMatrix, byRow.isSelected());
	}
	
}
