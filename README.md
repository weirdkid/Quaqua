Quaqua-JFC - pure Java JFileChooser for Mac
======

A fork of karlvr's Quaqua, which appears to be the official continuation of Werner Randelshofer's original work here: from http://www.randelshofer.ch/quaqua/

Mr. Randelshofer did a awesome job on Quaqua for many years, but then he had to let it go. Perhaps due to less interest in Java on Mac, Java Swing, or he just got bored. You can tell by looking at the code (through the lens of what code looked like during the Java 1.5-1.6 era) that Randdelshofer cared deeply about responsiveness and accuracy of his look and feel. 

I'd been using the Quaqua JFileChooser for years. It's hands-down THE BEST JFileChooser component for Mac. The default Swing one is barely unusable for Mac users, so if any part of Quaqua needs to live on, it's this.

However, it had been aging not so gracefully. It relied on native code that sometimes crashed on new versions of macOS. It used outdated system resources to populate the sidebar. It really hasn't been substantively updated since at least macOS 10.9 (Mavericks).

My goal here is to try to morph Randelshofer's JFC into a pure Java implementation that works good enough on all Macs. Randelshofer put a lot of effort into making this component look exactly like the native one, but I'm going more for "close enough to native" but still leaps and bounds better than the Swing default.

Now that JavaFX is being spun out of Java, some may yet still find this useful.

Compile using ant:
ant -f build-quaqua.xml jar-filechooser


Code notes:
-----------

- There are variations of the UI for every version of macOS up through Mavericks, but the last one to not rely on native code was the QuaquaLeopardFileChooserUI, so that's where my effort is focused. The QuaquaManager only provides this option for UI now.
- QuaQuaFileSystemView picks a macOS version-specific implementation of a FileSystemView. Ever since Lion, they all use OSXLionFileSystemView, so that's also where I focus.

