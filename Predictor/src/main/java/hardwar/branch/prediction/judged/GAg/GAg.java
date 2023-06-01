package hardwar.branch.prediction.judged.GAg;
import hardwar.branch.prediction.shared.*;import hardwar.branch.prediction.shared.devices.*;
import java.util.Arrays;
public class GAg implements BranchPredictor {
    private final ShiftRegister BHR; // branch history register
    private final Cache<Bit[], Bit[]> PHT; // page history table
    private final ShiftRegister SC; // saturated counter register
    public GAg() {        
        this(4, 2);
    }
    /**     * Creates a new GAg predictor with the given BHR register size and initializes the BHR and PHT.
     *     * @param BHRSize the size of the BHR register
     * @param SCSize  the size of the register which hold the saturating counter value and the cache block size     */
    public GAg(int BHRSize, int SCSize) {        // TODO : complete the constructor
        // Initialize the BHR register with the given size and no default value
        this.BHR = new SIPORegister("bhr", BHRSize, null );
        // Initialize the PHT with a size of 2^size and each entry having a saturating counter of size "SCSize"
        this.PHT = new PageHistoryTable((int)Math.pow(2, BHRSize), SCSize);
        // Initialize the SC register
        this.SC =  new SIPORegister("sc", SCSize ,null);
    }
    /**     * Predicts the result of a branch instruction based on the global branch history
     *     * @param branchInstruction the branch instruction
     * @return the predicted outcome of the branch instruction (taken or not taken)     */
    @Override    public BranchResult predict(BranchInstruction branchInstruction) {
        Bit[] bhrValue = this.BHR.read();
        Bit[] readBlock = this.PHT.get(bhrValue);
        this.SC.load(readBlock); 
        return BranchResult.of(readBlock[0].getValue());
    }
    
    public void update(BranchInstruction instruction, BranchResult actual) {
        if (BranchResult.isTaken(actual)){            this.SC.load(CombinationalLogic.count(this.SC.read(), true,CountMode.SATURATING));
        }        else {
        this.SC.load(CombinationalLogic.count(this.SC.read(), false,CountMode.SATURATING));    
        }
        this.BHR.insert(Bit.of(BranchResult.isTaken(actual)));
    }
    /**
     * @return a zero series of bits as default value of cache block     */
    private Bit[] getDefaultBlock() {       
        Bit[] defaultBlock = new Bit[SC.getLength()];
        Arrays.fill(defaultBlock, Bit.ZERO);        return defaultBlock;
    }
    public String monitor() {
        return "GAg predictor snapshot: \n" + BHR.monitor() + SC.monitor() + PHT.monitor();    }
    public static void main(String[] args) {
        GAg gag = new GAg(4, 2);    }
}