analyzer-korean
===============
fastcatsearch Korean analyzer project

## Building

1. Download source from github repository

    $ tar xzvf analyzer-2.xxx.tar.gz
    
2. Maven it

```
$ cd analyzer-2.xxx/
$ ls
  arirang		chinese		japanese	korean
$ cd korean
$ mvn install

    ...
    
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.216s
[INFO] Finished at: Mon Mar 10 10:49:29 KST 2014
[INFO] Final Memory: 19M/46M
[INFO] ------------------------------------------------------------------------
```

3. Install to fastcatsearch

```
$ cd target/
$ cd analyzer-korean-xxx/
$ cd plugin/
$ cd analysis/
$ ls 
  Korean
  
```
Copy analysis directory into a fastcatsearch's plugin dictrectory.

```
$ cp -r Korean [fastcatsearch_installed_dicrectory]/plugin/analysis/
```

4. Done

Start fastcatsearch and use Korean analyzer.
 

