package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.IntObjectOpenHashMap;

public abstract class CanonicalComparator {
	
	public static class TokenCanonicCode{
		
		public TokenCanonicCode(int id, int mainTokenId) {
			this.setId(id);
			this.mainTokenId = mainTokenId;
		}

		private int id;
		
		private int mainTokenId;
		
		private List<AttachmentCanonicCode> attachmentCodes;

		public int mainId() {
			return mainTokenId;
		}

		public List<AttachmentCanonicCode> getAttachmentCodes() {
			return attachmentCodes;
		}

		public void addAttachment(AttachmentCanonicCode attachment) {
			if(this.attachmentCodes==null){
				this.attachmentCodes = new ArrayList<AttachmentCanonicCode>();
			}
			attachmentCodes.add(attachment);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((attachmentCodes == null) ? 0 : attachmentCodes
							.hashCode());
			result = prime * result + mainTokenId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TokenCanonicCode other = (TokenCanonicCode) obj;
			if (attachmentCodes == null) {
				if (other.attachmentCodes != null)
					return false;
			} else if (!attachmentCodes.equals(other.attachmentCodes))
				return false;
			if (mainTokenId != other.mainTokenId)
				return false;
			return true;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
	}
	
	public static class AttachmentCanonicCode implements Comparable<AttachmentCanonicCode>{
		
		public AttachmentCanonicCode(int tokenType, TokenCanonicCode tokenCode) {
			this.tokenType = tokenType;
			this.tokenCode = tokenCode;
		}

		private int tokenType;
		
		private TokenCanonicCode tokenCode;
		
		public int id(){
			return tokenCode.mainId();
		}

		public int getTokenType() {
			return tokenType;
		}

		@Override
		public int compareTo(AttachmentCanonicCode o) {
			return this.id() - o.id();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((tokenCode == null) ? 0 : tokenCode.hashCode());
			result = prime * result + tokenType;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttachmentCanonicCode other = (AttachmentCanonicCode) obj;
			if (tokenCode == null) {
				if (other.tokenCode != null)
					return false;
			} else if (!tokenCode.equals(other.tokenCode))
				return false;
			if (tokenType != other.tokenType)
				return false;
			return true;
		}
	}
	
	private IntObjectOpenHashMap<TokenCanonicCode> codeMap = new IntObjectOpenHashMap<TokenCanonicCode>();
	
	public TokenCanonicCode getTokenCode(IToken token){
		int id = token.id();
		TokenCanonicCode code = codeMap.get(id);
		if(code==null){
			code = buildTokenCode(token);
			if(code.attachmentCodes!=null){
				Collections.sort(code.attachmentCodes);
			}
			codeMap.put(id, code);
		}
		return code;
	}
	
	protected abstract TokenCanonicCode buildTokenCode(IToken token);

}
