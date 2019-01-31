# LazyJSON
LazyJSON is a very fast JSON parser for Java that sprung out of the [StroomData](https://github.com/doubledutch/StroomData) project at DoubleDutch. You can read more about the origin of the library in [this blogpost](https://content.doubledutch.me/blog/json-parser).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.doubledutch/lazyjson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.doubledutch/lazyjson)
[![Javadocs](http://www.javadoc.io/badge/me.doubledutch/lazyjson.svg)](http://www.javadoc.io/doc/me.doubledutch/lazyjson)

## Usage

LazyJSON has an API based on that of the `org.json` Java library as demonstrated in the following sample:

```java
String source="{\"title\":\"World\"}";

LazyObject obj=new LazyObject(source);
System.out.println("Hello "+obj.getString("title")+"!");
// Expected output: "Hello World!"
````

The library includes a JSON compression feature that uses template based encoding and caching of repeated string values using an http2 header cache inspired scheme.

```java
String source="{\"title\":\"World\"}";
Compressor cmp=new Compressor("./compression_cache",100,1);
byte[] data=cmp.compress(source);
cmp.commit(); // Write out templates and dictionary data
String output=cmp.decompress(data);
````

In our own tests on real world data at DoubleDutch LazyJSON compression gets us down to around 20-25%. We are able to further get down to less than 2% by combining this with a general purpose compression tool such as xz. You can read more about the process and [our results here](http://engineering.doubledutch.me/h/i/282745449-lazyjson-and-the-chamber-of-extreme-compression).

## Performance

The following table shows a speed comparison for parsing around 551kb of JSON data.

JSON Library | Min | Max | Avg | Median
-------------|-----|-----|-----|-------
json.org | 7.75176 | 22.426924 | 11.096008 | 10.59668
GSON JsonParser | 3.703044 | 9.933935 | 5.780482 | 5.690398
Jackson ObjectMapper | 2.882437 | 8.55358 | 5.165173 | 4.887188
Jackson JsonParser | 1.982652 | 6.068085 | 3.27319225 | 3.2957
LazyJSON | 1.590227 | 3.763087 | 2.54875 | 2.566992
GSON class based | 4.14453 | 12.330691 | 6.4768755 | 6.16929
Boon | 4.100606 | 9.142838 | 5.808073 | 5.70455

However, the real speed benefit comes when using LazyJSON to extract JSON objects from a string representation of JSON array with the intention of getting each object as a separate string (this is the usage of this library in [StroomData](https://github.com/doubledutch/StroomData)). The following table shows the same test data as above processed in this fashion.

JSON Library | Min | Max | Avg | Median
-------------|-----|-----|-----|-------
json.org | 16.193432 | 34.084046 | 20.677518 | 19.336549
GSON JsonParser based | 7.254918 | 17.019365 | 10.777558 | 10.150436
Jackson ObjectMapper | 5.149789 | 13.676147 | 7.4314225 | 7.19378
LazyJSON | 1.545074 | 4.47108 | 2.73187325 | 2.778728
GSON class based | 8.921176 | 20.0559 | 12.446913 | 12.08505
Boon | 10.468966 | 25.870486 | 14.332774 | 13.447765

For more details about these tests, the source code and further benchmarks - see this repo [https://github.com/kasperjj/LazyJSONBenchmark](https://github.com/kasperjj/LazyJSONBenchmark)

## Design Principles

LazyJSON was initially designed as an index overlay parser that would collect tokens into a very simple AST while maintaining the index locations of the objects' start and end tokens. This functionality allows it to extract the substring containing an object from the source string data in a very very efficient manner—so efficient, in fact, that it is twice as fast as Jackson for that specific task.

The purpose behind this design was to be able to efficiently split a batch of data submitted to StroomData into separate strings for each object that could each be committed to storage separately. The following snippet shows this specific usage.

```java
String batch="[{\"foo\":1},{\"foo\":2},{\"foo\":3}]";

LazyArray array=new LazyArray(batch);
for(int i=0;i<array.length();i++){
	String data=array.getJSONObject(i).toString();
}
````

The initial benchmark results were so encouraging that they sparked the question - what if we turned this into a full featured JSON parser that would attempt to be as lazy as possible? That is, when it initially parses a source string it only does the most basic tokenization needed to validate the input. Everything else is postponed until you actually request data. Let's look at a sample of what this means.

```java
String source="{\"foo\":42}";

LazyObject obj=new LazyObject(source);
int i=obj.getInt("foo");
````

In this sample, the parser will validate the source string when we instantiate the LazyObject class, but it won't actually create an int from the value of the foo field until we request it.

The changes needed to accommodate this functionality had a neglible impact on the general performance of the parser, which yielded very promising results when compared to other high performance JSON parsers available in Java. In our initial testing, the raw parsing of JSON data has been much faster than all other JSON parsers to which we have compared it. As you start to access the data—and are thus forcing it to do the work that was skipped during the initial parsing—it gets slower and slower, but so far in our testing it still maintains a lead even when all fields are accessed.

## State of the Project

We are using LazyJSON in production at DoubleDutch. So far, it has been very stable and we are highly committed to fixing any issues that might arise in an extremely efficient manner. I repeat: we are using this in production! If you find any issues, please file them right here on the github project page!
