package qora.transaction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import database.DatabaseSet;
import qora.account.Account;
import qora.crypto.Base58;

public abstract class Transaction {
	
	//VALIDATION CODE
	public static final int VALIDATE_OKE = 1;
	public static final int INVALID_ADDRESS = 2;
	public static final int NEGATIVE_AMOUNT = 3;
	public static final int NEGATIVE_FEE = 4;
	public static final int NO_BALANCE = 5;
	public static final int INVALID_REFERENCE = 6;
	
	public static final int INVALID_NAME_LENGTH = 7;
	public static final int INVALID_VALUE_LENGTH = 8;
	public static final int NAME_ALREADY_REGISTRED = 9;
	
	public static final int NAME_DOES_NOT_EXIST = 10;
	public static final int INVALID_NAME_OWNER = 11;
	public static final int NAME_ALREADY_FOR_SALE = 12;
	public static final int NAME_NOT_FOR_SALE = 13;
	public static final int BUYER_ALREADY_OWNER = 14;
	public static final int INVALID_AMOUNT = 15;
	public static final int INVALID_SELLER = 16;
	
	public static final int NAME_NOT_LOWER_CASE = 17;
	
	//TYPES
	public static final int GENESIS_TRANSACTION = 1;
	public static final int PAYMENT_TRANSACTION = 2;
	
	public static final int REGISTER_NAME_TRANSACTION = 3;
	public static final int UPDATE_NAME_TRANSACTION = 4;
	public static final int SELL_NAME_TRANSACTION = 5;
	public static final int CANCEL_SELL_NAME_TRANSACTION = 6;
	public static final int BUY_NAME_TRANSACTION = 7;
	
	//MINIMUM FEE
	public static final BigDecimal MINIMUM_FEE = BigDecimal.ONE;
	
	//PROPERTIES LENGTH
	protected static final int TYPE_LENGTH = 4;
	public static final int TIMESTAMP_LENGTH = 8;
	protected static final int REFERENCE_LENGTH = 64;
		
	protected byte[] reference;
	protected BigDecimal fee;
	protected int type;
	protected byte[] signature;
	protected long timestamp;
	
	protected Transaction(int type, BigDecimal fee, long timestamp, byte[] reference, byte[] signature)
	{
		this.fee = fee;
		this.type = type;
		this.signature = signature;
		this.timestamp = timestamp;
		this.reference = reference;
	}
	
	//GETTERS/SETTERS
	
	public int getType()
	{
		return this.type;
	}
	
	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	public long getDeadline()
	{
		//24HOUR DEADLINE TO INCLUDE TRANSACTION IN BLOCK
		return this.timestamp + (1000*60*60*24);
	}
	
	public BigDecimal getFee()
	{
		return this.fee;
	}
	
	public byte[] getSignature()
	{
		return this.signature;
	}
	
	public byte[] getReference()
	{
		return this.reference;
	}
	
	public BigDecimal feePerByte()
	{
		return this.fee.divide(new BigDecimal(this.getDataLength()), MathContext.DECIMAL32);
	}
	
	public boolean hasMinimumFee()
	{
		return this.fee.compareTo(MINIMUM_FEE) >= 0;
	}
	
	//PARSE/CONVERT
	
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject transaction = new JSONObject();
		
		transaction.put("type", this.type);
		transaction.put("fee", this.fee.toPlainString());
		transaction.put("timestamp", this.timestamp);
		transaction.put("reference", Base58.encode(this.reference));
		transaction.put("signature", Base58.encode(this.signature));
		
		return transaction;
	}
	
	public abstract JSONObject toJson();
	
	public abstract byte[] toBytes();
	
	public abstract int getDataLength();
	
	//VALIDATE
	
	public abstract boolean isSignatureValid();
	
	public int isValid()
	{
		return this.isValid(DatabaseSet.getInstance());
	}
	
	public abstract int isValid(DatabaseSet db);
	
	//PROCESS/ORPHAN
	
	public void process()
	{
		this.process(DatabaseSet.getInstance());
	}
		
	public abstract void process(DatabaseSet db);

	public void orphan()
	{
		this.orphan(DatabaseSet.getInstance());
	}
	
	public abstract void orphan(DatabaseSet db);
	
	//REST
	
	public abstract Account getCreator();
	
	public abstract List<Account> getInvolvedAccounts();
		
	public abstract boolean isInvolved(Account account);
	
	public abstract BigDecimal getAmount(Account account);
	
	@Override 
	public boolean equals(Object object)
	{
		if(object instanceof Transaction)
		{
			Transaction transaction = (Transaction) object;
			
			return Arrays.equals(this.getSignature(), transaction.getSignature());
		}
		
		return false;
	}

	public boolean isConfirmed()
	{
		return this.isConfirmed(DatabaseSet.getInstance());
	}
	
	public boolean isConfirmed(DatabaseSet db)
	{
		return DatabaseSet.getInstance().getTransactionParentDatabase().contains(this);
	}
	
	public int getConfirmations()
	{
		//CHECK IF IN TRANSACTIONDATABASE
		if(DatabaseSet.getInstance().getTransactionsDatabase().contains(this))
		{
			return 0;
		}
		
		//CALCULATE CONFIRMATIONS
		int lastBlockHeight = DatabaseSet.getInstance().getHeightDatabase().getHeightBySignature(DatabaseSet.getInstance().getBlockDatabase().getLastBlockSignature());
		int transactionBlockHeight = DatabaseSet.getInstance().getHeightDatabase().getHeightBySignature(DatabaseSet.getInstance().getTransactionParentDatabase().getParentSignature(this));
		
		//RETURN
		return 1 + lastBlockHeight - transactionBlockHeight;
	}

}
