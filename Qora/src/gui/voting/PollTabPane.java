package gui.voting;

import gui.Gui;
import gui.models.VotesTableModel;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import qora.voting.Poll;
import utils.BigDecimalStringComparator;

public class PollTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private PollDetailsPanel pollDetailsPanel;
		
	@SuppressWarnings("unchecked")
	public PollTabPane(Poll poll)
	{
		super();
			
		//POLL DETAILS
		this.pollDetailsPanel = new PollDetailsPanel(poll);
		this.addTab("Poll Details", this.pollDetailsPanel);
		
		//ALL VOTES
		VotesTableModel allVotesTableModel = new VotesTableModel(poll.getVotes());
		final JTable allVotesTable = Gui.createSortableTable(allVotesTableModel, 0);
		
		TableRowSorter<VotesTableModel> sorter =  (TableRowSorter<VotesTableModel>) allVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab("All Votes", new JScrollPane(allVotesTable));
		
		//MY VOTES
		VotesTableModel myVotesTableModel = new VotesTableModel(poll.getVotes(Controller.getInstance().getAccounts()));
		final JTable myVotesTable = Gui.createSortableTable(myVotesTableModel, 0);
		
		sorter =  (TableRowSorter<VotesTableModel>) myVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab("My Votes", new JScrollPane(myVotesTable));
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
	}
	
}
