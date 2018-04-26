package speeches.speech_generator_UCT;

import data_info.DataAnalysis;
import speeches.SpeechRelation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Class of tree node in the Monte Carlo tree */
public class TreeNode {

    // All columns (note the element in the list is SINGLE column speech)
    private static List<SpeechRelation> allColumns;

    // Generate a random integer
    private static Random random = new Random();
    private static int sampleSize = 10;
    // Not sure what's this for
    private static double epsilon = 1e-6;
    // The maximum number of columns in a speech
    private static int maxCol = 3;

    // The list of possible child nodes
    private List<TreeNode> children;
    // The speech of the current node
    private SpeechRelation speech;
    /* The list of columns in the speech
     * Note the element in the list is SINGLE column speech
     * Merge all single columns together we can get the speech */
    private List<SpeechRelation> columns;

    // The number of tuples that the current speech predicts correctly
    private double score;
    // The number of tuples that the current speech predicted on
    private double numVisit;
    // The data information
    private static DataAnalysis data;
    // The depth of the current node in the tree
    private int depth;

    /**
     * Constructor function
     * This would be used ONLY for root
     * */
    public TreeNode(int depth) {
        // Initialize all fields
        numVisit = 0;
        score = 0;
        children = new ArrayList<TreeNode>();
        columns = new ArrayList<SpeechRelation>();
        this.depth = depth;
    }

    /**
     * Constructor function
     * This would be used for nodes except root
     * */
    public TreeNode(int depth, List<SpeechRelation> columns) {
        numVisit = 0;
        score = 0;
        children = new ArrayList<TreeNode>();
        this.columns = columns;
        speech = new SpeechRelation(columns);
        this.depth = depth;
    }

    /* Check if the current node is leaf node or not */
    private boolean ifLeaf() {
        return children == null || children.size() == 0;
    }

    /*
     * Expand the current node
     * If there's not enough information to select, expand the current node
     * Note that we only expand the node when doesn't exceed the maximum limit
     * */
    private void expand() {
        if(depth < TreeNode.maxCol && columns.size() == depth) {
            List<SpeechRelation> nextColumns = getAllPossibleColumns(this.columns);

            // Generate all possible child nodes
            for (SpeechRelation nextColumn : nextColumns) {
                List<SpeechRelation> childList = new ArrayList<SpeechRelation>(columns);
                childList.add(nextColumn);
                children.add(new TreeNode(depth + 1, childList));
            }

            children.add(new TreeNode((depth + 1), columns));
        }
    }

    /** Setter function of all columns */
    public static void setAllColumns(List<SpeechRelation> allColumns) {
        TreeNode.allColumns = new ArrayList<SpeechRelation>(allColumns);
    }

    /* Get all possible columns except used ones */
    private List<SpeechRelation> getAllPossibleColumns(List<SpeechRelation> usedColumns) {
        List<SpeechRelation> result = new ArrayList<SpeechRelation>();

        for(SpeechRelation singleSpeech : TreeNode.allColumns) {
            if(!usedColumns.contains(singleSpeech)) {

                int i = 0;
                for(; i < usedColumns.size(); i++) {
                    if(singleSpeech.speech.columns.get(0) == usedColumns.get(i).speech.columns.get(0)) {
                        break;
                    }
                }

                // Add a copy of the current node as a leaf node
                if(i == usedColumns.size()) {
                    result.add(singleSpeech);
                }
            }
        }

        return result;
    }

    /** Setter function of the data used */
    public static void setData(DataAnalysis data) {
        TreeNode.data = data;
    }

    /* Function to do the simulation on the current node */
    private int simulate() {
        // Do the competing
        return TreeNode.data.compete(TreeNode.sampleSize, this.speech);
    }

    /* Function to update the score of the current node */
    private void updateState(int correctTuple) {
        numVisit++;
        score += (double) correctTuple / (double) TreeNode.sampleSize;
    }

    /* Select the most promising child node based on UCT */
    private TreeNode select() {
        TreeNode selectedNode = null;

        double maxValue = Double.MIN_VALUE;
        for(TreeNode child : children) {
            double uctValue = child.score / (child.numVisit + epsilon) +
                    Math.sqrt(Math.log(numVisit + 1) / (child.numVisit + epsilon)) + random.nextDouble() * epsilon;
            if(uctValue > maxValue) {
                selectedNode = child;
                maxValue = uctValue;
            }
        }

        return selectedNode;
    }

    /** Run the algorithm for one time */
    public void selectAction() {
        // The path down to a leaf
        List<TreeNode> visited =new LinkedList<TreeNode>();
        TreeNode cur = this;
        visited.add(this);

        // Expand the path down to a leaf
        while(!cur.ifLeaf()) {
            // Select the most promising child node and add it to the list
            cur = cur.select();
            visited.add(cur);
        }

        // The condition of exiting the loop is the node is leaf node
        cur.expand();
        // TODO: not sure we need the following sentences
//        TreeNode newNode = cur.select();
//        visited.add(newNode);

        // Do a simulation on the current node
        int value = cur.simulate();
        // Update the scores on the path
        for (TreeNode node :visited) {
            node.updateState(value);
        }
    }

    /** Get the optimal leaf node under the current node */
    public SpeechRelation getOptimal() {

        TreeNode curNode = this;
        List<TreeNode> optimalPath = new ArrayList<TreeNode>();

        // Greedy algorithm to choose the optimal leaf node
        while(curNode != null && !curNode.ifLeaf()) {

            TreeNode nextNode = null;
            double bestScore = 0;

            for(TreeNode child : curNode.children) {
                if(child.score / child.numVisit > bestScore) {
                    bestScore = child.score / child.numVisit;
                    nextNode = child;
                }
            }

            optimalPath.add(nextNode);
            curNode = nextNode;
        }

        // TODO: is it necessary to choose the optimal on the path or the leaf node would be sufficient?
        // Find the optimal value on the path
        double bestScore = 0;
        SpeechRelation bestSpeech = null;
        for(TreeNode node : optimalPath) {
            if(node.score / node.numVisit > bestScore) {
                bestScore = node.score / node.numVisit;
                bestSpeech = node.speech;
            }
        }

        return bestSpeech;
    }
}
