#HashMap Vs. ConcurrentHashMap Vs. SynchronizedMap – How a HashMap can be Synchronized in Java :
https://crunchify.com/hashmap-vs-concurrenthashmap-vs-synchronizedmap-how-a-hashmap-can-be-synchronized-in-java/

#HashTable Vs. HashMap
https://www.quora.com/What-is-the-difference-between-HashSet-HashMap-and-hash-table-How-do-they-behave-in-a-multi-threaded-environment

HashTable
- It does not allow null for both key and value. It will throw NullPointerException.
- Hashtable does not maintain insertion order. The order is defined by the Hash function. So only use this if you do not need data in order.
- It is synchronized. It is slow. Only one thread can access in one time.
- HashTable rea thread safe.
- HashTable uses Enumerator to iterate through elements.

HashMap
* It allows null for both key and value.
* HashMap does not maintain insertion order. The order is defined by the Hash function.
* It is not synchronized. It will have better performance.
* HashMap are not thread safe, but you can use Collections.synchronizedMap(new HashMap<K,V>())

TreeMap is an example of a SortedMap, which means that the order of the keys can be sorted, and when iterating over the keys, you can expect that they will be in order.
HashMap on the other hand, makes no such guarantee. Therefore, when iterating over the keys of a HashMap, you can't be sure what order they will be in.
HashMap will be more efficient in general, so use it whenever you don't care about the order of the keys.


