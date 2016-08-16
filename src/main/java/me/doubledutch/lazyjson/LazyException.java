package me.doubledutch.lazyjson;

/**
 * Exception used to indicate a parse or access error for LazyObject and LazyArray
 */
public final class LazyException extends RuntimeException{
	private int position=-1;
	private String message;
	
	public LazyException(String str){
		super(str);
		this.message=str;
	}

	public LazyException(String str,int position){
		super(str);
		this.position=position;
		this.message=str;
	}

	public LazyException(String str,LazyNode node){
		super(str);
		this.position=node.startIndex;
		this.message=str;
	}

	public String toString(){
		if(position>-1){
			return "@"+position+":"+message;
		}
		return message;
	}
}