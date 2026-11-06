# Mac verification: pick-up prompt

Continuation of the Java 17+ compatibility work done on Linux (Nov 2026).
Everything buildable/runnable on Linux has been verified. What remains is the
Mac-only runtime paths, which need a real Mac.

## Status

- Library compiles cleanly (zero warnings) on JDK 11/17/21/25 with `maven.compiler.release=11`
- `ChooserHarness` (auto-closes after 6s) passes on Linux JDK 11/17/21/25:
  LAF install, QuaquaLeopardFileChooserUI, JBrowser, FileSystemView all initialize
- Audited all reflection sites: active path uses public APIs only.
  `sun.swing.SwingUtilities2` = dead code. `NSApplication` reflection is Lion/JSheet-only
  (inactive) and wrapped in catch(Throwable).
- Known dead-on-modern-JDKs (not wired in, left as-is): Quaqua16LionFileChooserLAF,
  QuaquaPantherFileChooserLAF, QuaquaMavericksFileChooserLAF hardcode
  `apple.laf.AquaLookAndFeel` (Apple Java 6 class) and throw InternalError in their
  constructors. QuaquaManager only returns the Leopard LAF, so this is inert.

## On the Mac, do this

1. Build:
   ```
   mvn clean package -Dgpg.skip=true
   ```
   Expect zero project warnings.

2. Run the harness on each of JDK 17 and JDK 21 (use the full path to the right
   java binary, or set JAVA_HOME). A chooser window appears for ~6s then the app exits:
   ```
   mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/quaqua-cp.txt
   java -cp "target/classes:$(cat /tmp/quaqua-cp.txt)" com.weirdkid.quaqua.test.ChooserHarness
   ```

3. Check the console output for ANY of:
   - "WARNING: An illegal reflective access" (should not appear on 17+; would appear on 9-16)
   - "Quaqua Warning: Couldn't invoke NSApplication.requestUserAttention"
   - stack traces, "Unable to instantiate target Look and Feel", missing-icon NPEs
   - visually: sidebar should show native Mac icons (Documents, Downloads, ...),
     not blanks. If sidebar icons are blank, the CoreTypes.bundle .icns paths
     may have moved on the current macOS version - that is a macOS issue, not a
     Java version issue.

4. Repeat the harness with the Lion LAF to confirm it is inert/broken as documented:
   ```
   java -cp <same as above> com.weirdkid.quaqua.test.ChooserHarness \
     ch.randelshofer.quaqua.subset.Quaqua16LionFileChooserLAF
   ```
   Expected: InternalError "Unable to instantiate target Look and Feel" - confirms
   the apple.laf landmine. (Optional: fix these three LAFs to proxy the system LAF
   like QuaquaLeopardFileChooserLAF does, if the Lion UI is ever wanted back.)

5. If step 3 is clean: run Emailchemy itself on JDK 17+ and open its file
   chooser dialogs (open + save). That is the real acceptance test.

## Prompt to paste into a new session on the Mac

```
This is the Quaqua-JFC repo (pure Java Mac JFileChooser, used by Emailchemy).
We are verifying Java 17+ compatibility; Linux-side verification is done and
committed. Read MAC-TEST-PROMPT.md first, then:
1. mvn clean package -Dgpg.skip=true - confirm zero project warnings
2. Run com.weirdkid.quaqua.test.ChooserHarness under JDK 17 and JDK 21,
   capture full console output, confirm no illegal-access warnings or stack traces
3. Visually confirm the chooser sidebar shows native Mac icons
4. Report what you saw; if anything fails, diagnose and fix it
```
