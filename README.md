# LazyJSON
LazyJSON is a very fast JSON parser for Java that sprung out of the StroomData project at DoubleDutch. It has an API based on that of the  org.json Java library.

```java
String source="{\"title\":\"World\"}";

LazyObject obj=new LazyObject(source);
System.out.println("Hello "+obj.getString("title")+"!");
// Expected output: "Hello World!"
````

## Design Principles

LazyJSON was initially designed as an index overlay parser that would collect tokens into a very simple AST while maintain the index locations of the objects start and end tokens. This let it extract the substring containing an object from the source string data in a very very efficient manner. So efficient that it is twice as fast as Jackson for that specific task.

The purpose behind this design was to be able to efficiently split a batch of data submitted to StroomData into separate strings for each object that could each be commited to storage separately. The following snippet shows this specific usage:

```java
String batch="[{\"foo\":1},{\"foo\":2},{\"foo\":3}]";

LazyArray array=new LazyArray(batch);
for(int i=0;i<array.length();i++){
	String data=array.getJSONObject(i).toString();
}
````

The initial benchmark results were so encouraging that they sparked the question - what if we turned this into a full featured JSON parser that would attempt to be as lazy as possible. That is, when it initially parses a source string it only does the most basic tokenization needed to validate the input, everything else is postponed until you actually request data. Let's look at a sample of what this means.

```java
String source="{\"foo\":42}";

LazyObject obj=new LazyObject(source);
int i=obj.getInt("foo");
````

In this sample, the parser will validate the source string when we instantiate the LazyObject class, but it won't actually create an int from the value of the foo field until we request it.

The changes needed to accomodate this functionality had a neglible impact on the general performance of the parser and shows very promising results when compared to other high performance JSON parsers available in Java. In our initial testing, the raw parsing of JSON data is much faster than all other JSON parsers we have compared it to. As you start accessing the data - and are thus forcing it into doing the work that was skipped as it did the initial parsing - it gets slower and slower, but so far in our testing still maintains a lead even when all fields are accessed.

## State of the Project

We are using LazyJSON in production at DoubleDutch. It has so far been very stable and we are highly comitted to fixing any issues that might come up in an extremely efficient manner. As I said.... we are using this in production! If you find any issues, please file them right here on the github project page!

If you need strong validation of raw JSON input data at the edge of your stack - we would suggest using another JSON parser for now (we use Jackson). Not that we don't have confidence in LazyJSON, but simply that all current unit tests cover cases where the source JSON is well formed. We do intent to start adding test cases for malformed JSON to verify the validation abilities of the parser and will update this page when that happens!