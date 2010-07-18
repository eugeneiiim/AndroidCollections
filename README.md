This is a straightforward port of the Google Guava library (including
Google Collections) to Android.  *Most of the code has not yet been
tested for performance or correctness on an Android device*, so use at
your own risk.

How to use this library in Eclipse:
1. Clone the source code.
2. Import the library into Eclipse. File -> Import..., choose the source directory, and select "AndroidCollections".
3. Right click the project to use the library and select "Properties".
4. Select the "Android" section, click "Add...", and select the "AndroidCollections" project.

Changes to Google Collections:
1. References to javax.annotations.Nullable, javax.annotations.GuardedBy, and javax.annotations.ParametersAreNonnullByDefault were replaced with dummy annotations: androidcollections.annotations.Nullable, androidcollections.annotations.GuardedBy, and androidcollections.annotations.ParametersAreNonnullByDefault.
2. Imports in all files were updated to work with the Android library.
3. @Override annotations were removed where Eclipse complained about them.
