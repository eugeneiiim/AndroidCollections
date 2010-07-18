This is a straightforward port of the Google Guava library (including
Google Collections) (http://code.google.com/p/guava-libraries) to
Android.  __Most of the code has not yet been tested for performance
or correctness on an Android device, so use at your own risk__.

How to use this library in Eclipse:

* Clone the source code.

* Import the library into Eclipse. File -> Import..., choose the source directory, and select "AndroidCollections".

* Right click the project to use the library and select "Properties".

* Select the "Android" section, click "Add...", and select the "AndroidCollections" project.

Changes to Google Collections:

* References to javax.annotations.Nullable, javax.annotations.GuardedBy, and javax.annotations.ParametersAreNonnullByDefault were replaced with dummy annotations: androidcollections.annotations.Nullable, androidcollections.annotations.GuardedBy, and androidcollections.annotations.ParametersAreNonnullByDefault.

* Imports in all files were updated to work with the Android library.

* @Override annotations were removed where Eclipse complained about them.
