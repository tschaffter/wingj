/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under a Creative Commons Attribution
-NonCommercial 3.0 Unported License. Please cite the papers listed on 
http://lis.epfl.ch/wingj when using WingJ in your publication.

For commercial use, please contact Thomas Schaffter 
(thomas.schaff...@gmail.com).

A brief description of the license is available at 
http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
http://creativecommons.org/licenses/by-nc/3.0/legalcode.

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
 * loader will 'load' items by calling thingToDo for each item,
 * before calling allDone when all the things to do have been done.
 * Reference: http://stackoverflow.com/questions/8682085/can-i-sync-up-multiple-image-onload-calls
 */
function loader(items, images, thingToDo, allDone) {
    if (!items) {
        // nothing to do.
        return;
    }

    if ("undefined" === items.length) {
        // convert single item to array.
        items = [items];
    }

    var count = items.length;

    // this callback counts down the things to do.
    var thingToDoCompleted = function (items, i) {
        count--;
        if (0 == count) {
            allDone(items);
        }
    };

    for (var i = 0; i < items.length; i++) {
        // 'do' each thing, and await callback.
        thingToDo(items, images, i, thingToDoCompleted);
    }
}

// ----------------------------------------------------------------------------

/** Called after loading each image. */
function loadImage(items, images, i, onComplete) {

	// called only if the image has been successfully loaded
    var onLoad = function(e) {
        e.target.removeEventListener("load", onLoad);
		images[i] = e.target;
        // notify that we're done.
        onComplete(items, i);
    }
	
	// called when image has NOT been successfully loaded
	var onError = function(e) {
		e.target.removeEventListener("error", onError);
		images[i] = null;
        // notify that we're done.
        onComplete(items, i);
	
	}
	
    var img = new Image();
    img.addEventListener("load", onLoad, false);
	img.addEventListener("error", onError, false);
    img.src = items[i];
}

// var items = ['http://bits.wikimedia.org/images/wikimedia-button.png',
             // 'http://bits.wikimedia.org/skins-1.18/common/images/poweredby_mediawiki_88x31.png',
             // 'http://upload.wikimedia.org/wikipedia/en/thumb/4/4a/Commons-logo.svg/30px-Commons-logo.svg.png',
             // 'http://upload.wikimedia.org/wikipedia/commons/3/38/Icons_example.png'];

// loader(items, loadImage, function () {
    // alert("done");
// });