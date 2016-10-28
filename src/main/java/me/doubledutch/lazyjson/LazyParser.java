package me.doubledutch.lazyjson;

import java.util.*;

public final class LazyParser{
	// Read the comments on push before changing these!
	private final int STACK_INCREASE=31;
	private int STACK_SIZE=32;

	protected LazyNode root;
	protected final char[] cbuf;
	protected final int length;
	private int n=0;

	protected LazyParser(final String source){
		length=source.length();
		cbuf=new char[length];
		source.getChars(0,length,cbuf,0);
	}

	// The parser uses a crude stack while parsing that maintains a reference
	// to the top element on the stack and automatically establishes a parent
	// child relation ship when elements are pushed onto the stack.
	private LazyNode[] stack=new LazyNode[STACK_SIZE];
	private LazyNode stackTop=null;
	private int stackPointer=1;

	// Push a token onto the stack and attach it to the previous top as a child
	private void push(final LazyNode token){
		stackTop.addChild(token);
		// The stack allocation strategy here is to increase it in increments
		// of a power of two. This lets us check when all low bits are set and
		// its time to increase the stack again.
		// This lets us do a compare between a constant and a variable instead
		// of between two variables.
		if((stackPointer & STACK_INCREASE)==STACK_INCREASE){
			LazyNode[] newStack=new LazyNode[STACK_SIZE+STACK_INCREASE+1];
			System.arraycopy(stack,0,newStack,0,STACK_SIZE);
			STACK_SIZE=STACK_SIZE+STACK_INCREASE+1;
			stack=newStack;
		}
		stack[stackPointer++]=token;
		stackTop=token;
	}

	// Pop a token off the stack and reset the stackTop pointer
	private LazyNode pop(){
		LazyNode value=stackTop;
		stackPointer--;
		if(stackPointer>0){
			stackTop=stack[stackPointer-1];
		}
		return value;
	}

	// Pop a token off the stack and reset the stackTop pointer without returning the value
	private void drop(){
		stackPointer--;
		stackTop=stack[stackPointer-1];
	}

	private int size(){
		return stackPointer-1;
	}

	// Utility method to consume sections of whitespace
	private final void consumeWhiteSpace(){
		char c=cbuf[n];
		while(c==' '|| c=='\n' || c=='\t' || c=='\r'){
			n++;
			c=cbuf[n];
		}
	}

	// Attempt to advance and consume any whitespace
	// Roll back the index counter to prepare for another iteration
	// through the main loop afterwards.
	private final void tryToConsumeWhiteSpace(){
		n++;
		consumeWhiteSpace();
		n--;
	}

	// Consume all characters in a string and correctly mark the stackTop
	// element if an escape character is found
	private final boolean consumeString() throws LazyException{
		boolean escaped=false;
		n++;
		char c=cbuf[n];
		while(c!='"'){
			if(c=='\\'){
				n++;
				c=cbuf[n];
				if(!(c=='"' || c=='\\' || c=='/' || c=='b' || c=='f' || c=='n' || c=='r' || c=='t' || c=='u')){
					throw new LazyException("Invalid escape code",n);
				}
				escaped=true;
			}
			n++;
			c=cbuf[n];
		}
		return escaped;
	}

	// Consume all characters in a number and throw an exception if the format
	// of the number does not validate correctly
	private final boolean consumeNumber(char c) throws LazyException{
		boolean floatChar=false;
		if(c=='-'){
			// If the number started with a minus sign it must be followed by at least one digit
			n++;
			c=cbuf[n];
			if(c<'0' || c>'9'){
				throw new LazyException("Digit expected",n);
			}
		}
		n++;
		if(c=='0'){
			// First digit was zero - next may not be digit!
			c=cbuf[n];
			if(c>='0' && c<='9'){
				throw new LazyException("Number may not start with leading zero",n);
			}
		}else{
			c=cbuf[n];
		}
		while(!(c<'0' || c>'9')){
			n++;
			c=cbuf[n];
		}
		if(c=='.'){
			floatChar=true;
			// The fractional part must contain one or more digits
			n++;
			c=cbuf[n];
			if(c<'0' || c>'9'){
				throw new LazyException("Digit expected",n);
			}
			n++;
			c=cbuf[n];
			while(!(c<'0' || c>'9')){
				n++;
				c=cbuf[n];
			}
		}
		if(c=='e' || c=='E'){
			floatChar=true;
			n++;
			c=cbuf[n];
			if(c=='-' || c=='+'){
				// We must have at least one digit following this
				n++;
				c=cbuf[n];
				if(c<'0' || c>'9'){
					throw new LazyException("Digit expected",n);
				}
			}else if(c<'0' || c>'9'){
				throw new LazyException("Exponential part expected",n);
			}
			n++;
			c=cbuf[n];
			while(!(c<'0' || c>'9')){
				n++;
				c=cbuf[n];
			}
		}
		return floatChar;
	}

	// This should probably be renamed to parse. This method started out as a
	// simple index overlay tokenizer, but then slowly evolved into a full
	// parser.
	//
	// It works by iterating over all characters in the source and switching
	// based on token type to consume full tokens. It maintains a simple
	// stack to both validate the structure and to be able to provide the
	// abstract syntax tree in the form of linked LazyNodes after parsing.
	//
	// The source is ugly - but it's fast.... very fast
	// There is still plenty of room for optimization, such as when a value
	// is put on the stack, consumed and then pulled of the stack immediately
	// again. While it is consistent and readable, it needlesly maintains the
	// stackTop pointer and increments and decrements the stackTopPointer int
	protected void tokenize() throws LazyException{
		consumeWhiteSpace();
		// We are going to manually push the first token onto the stack so
		// future push operations can avoid doing an if empty check when
		// setting the parent child relationship
		char c=cbuf[n];
		if(c=='{'){
			stack[stackPointer++]=LazyNode.cObject(n);
		}else if(c=='['){
			stack[stackPointer++]=LazyNode.cArray(n);
		}else{
			throw new LazyException("Can not parse raw JSON value, must be either object or array",0);
		}
		root=stack[1];
		stackTop=root;
		n++;
		boolean expectValue=false;
		boolean firstValue=true;
		LazyNode token=null;
		for(;n<length;n++){
			c=cbuf[n];
			switch(c){
				case '{':
					push(LazyNode.cObject(n));
					expectValue=false;
					firstValue=true;
					break;
				case '}':
					// The end of an object, pop off the last value and field if any
					token=pop();
					if(token==null){
						throw new LazyException("Unexpected end of object character",n);
					}else if(token.type!=LazyNode.OBJECT){
						throw new LazyException("Unexpected end of object character",n);
					}
					token.endIndex=n+1;
					// If this object was the value for a field, pop off that field too
					if(stackTop!=null && (stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD)){
						drop();
					}
					// Was there a trailing comma?
					if(expectValue){
						throw new LazyException("Unexpected comma without another value",n-1);
					}
					firstValue=false;
					break;
			case '"':
				expectValue=false;
				firstValue=false;
				if(stackTop.type==LazyNode.ARRAY){
					token=LazyNode.cStringValue(n+1);
					stackTop.addChild(token);
					if(consumeString()){
						token.type=LazyNode.VALUE_ESTRING;
					}
					token.endIndex=n;
				}else if(stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD){
					token=LazyNode.cStringValue(n+1);
					stackTop.addChild(token);
					if(consumeString()){
						token.type=LazyNode.VALUE_ESTRING;
					}
					token.endIndex=n;
					drop();
				}else if(stackTop.type==LazyNode.OBJECT){
					push(LazyNode.cField(n+1));
					if(consumeString()){
						stackTop.type=LazyNode.EFIELD;
					}
					stackTop.endIndex=n;
					n++;
					consumeWhiteSpace();
					c=cbuf[n];
					if(c==':'){
						tryToConsumeWhiteSpace();
					}else{
						throw new LazyException("Unexpected character! Was expecting field separator ':'",n);
					}
					expectValue=true;
				}
				break;
			case ',':
				// This must be the end of a value and the start of another
				if(expectValue){
					// Missing value after last comma
					throw new LazyException("Unexpected comma",n);
				}
				expectValue=true;
				if(stackTop!=null && (stackTop.type==LazyNode.ARRAY) && (firstValue)){
					throw new LazyException("Expected value before comma",n);
				}
				break;
			case '[':
				if(stackTop.type==LazyNode.OBJECT){
					throw new LazyException("Missing field name for array",n);
				}else if(stackTop.type==LazyNode.ARRAY){
					if((!firstValue) && (!expectValue)){
						throw new LazyException("Nested array without comma",n);
					}
				}
				push(LazyNode.cArray(n));
				expectValue=false;
				firstValue=true;
				break;
			case ']':
				token=pop();
				if(token==null){
						throw new LazyException("Unexpected end of array character",n);
				}else if(token.type!=LazyNode.ARRAY){
					if(token.endIndex==-1){
						token.endIndex=n;
					}
					token=pop();
					if(token==null || token.type!=LazyNode.ARRAY){
						throw new LazyException("Unexpected end of array",n);
					}
				}
				token.endIndex=n+1;
				// If this array was the value for a field, pop off that field too
				if(stackTop!=null && (stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD)){
					drop();
				}
				// Was there a trailing comma?
				if(expectValue){
					throw new LazyException("Unexpected comma without another value",n-1);
				}
				firstValue=false;
				break;
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				// Ignore white space characters here
				break;
			default:
				// This must be a new value
				if(stackTop.type==LazyNode.ARRAY && !firstValue && !expectValue){
					throw new LazyException("Unexpected value, missing comma?",n);
				}
				firstValue=false;
				expectValue=false;
				if(c=='n'){
					// Must be null value
					if(cbuf[++n]=='u' && cbuf[++n]=='l' && cbuf[++n]=='l'){
						token=LazyNode.cValueNull(n);
						stackTop.addChild(token);
						token.endIndex=n;
						if(stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD){
							// This was the end of the value for a field, pop that too
							drop();
						}
					}else{
						throw new LazyException("Syntax error",n);
					}
				}else if(c=='t'){
					// Must be true value
					if(cbuf[++n]=='r' && cbuf[++n]=='u' && cbuf[++n]=='e'){
						token=LazyNode.cValueTrue(n);
						stackTop.addChild(token);
						token.endIndex=n;
						if(stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD){
							// This was the end of the value for a field, pop that too
							drop();
						}
					}else{
						throw new LazyException("Syntax error",n);
					}
				}else if(c=='f'){
					// Must be false value
					if(cbuf[++n]=='a' && cbuf[++n]=='l' && cbuf[++n]=='s' && cbuf[++n]=='e'){
						token=LazyNode.cValueFalse(n);
						stackTop.addChild(token);
						token.endIndex=n;
						if(stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD){
							// This was the end of the value for a field, pop that too
							drop();
						}
					}else{
						throw new LazyException("Syntax error",n);
					}
				}else if(c=='-' || !(c<'0' || c>'9')){
					// Must be a number
					token=LazyNode.cNumberValue(n);
					stackTop.addChild(token);
					if(consumeNumber(c)){
						token.type=LazyNode.VALUE_FLOAT;
					}
					token.endIndex=n;
					n--;
					if(stackTop.type==LazyNode.FIELD || stackTop.type==LazyNode.EFIELD){
						// This was the end of the value for a field, pop that too
						drop();
					}
				}else{
					throw new LazyException("Syntax error",n);
				}				
			
				break;
			}
		}
		if(size()!=0){
			throw new LazyException("Unexpected end of JSON data");
		}
		if(expectValue){
			throw new LazyException("Unexpected trailing comma");
		}
	}
}