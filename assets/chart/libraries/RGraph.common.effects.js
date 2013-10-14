    /**
    * o------------------------------------------------------------------------------o
    * | This file is part of the RGraph package - you can learn more at:             |
    * |                                                                              |
    * |                          http://www.rgraph.net                               |
    * |                                                                              |
    * | This package is licensed under the RGraph license. For all kinds of business |
    * | purposes there is a small one-time licensing fee to pay and for non          |
    * | commercial  purposes it is free to use. You can read the full license here:  |
    * |                                                                              |
    * |                      http://www.rgraph.net/license                           |
    * o------------------------------------------------------------------------------o
    */
    
    /**
    * This is a library of a few functions that make it easier to do
    * effects like fade-ins or eaxpansion.
    */

    /**
    * Initialise the various objects
    */
    if (typeof(RGraph) == 'undefined') RGraph = {isRGraph:true,type:'common'};
    
    RGraph.Effects = {};
    RGraph.Effects.Fade             = {}; RGraph.Effects.jQuery           = {}
    RGraph.Effects.jQuery.HBlinds   = {}; RGraph.Effects.jQuery.VBlinds   = {}
    RGraph.Effects.jQuery.Slide     = {}; RGraph.Effects.Pie              = {}
    RGraph.Effects.Bar              = {}; RGraph.Effects.Line             = {}
    RGraph.Effects.Line.jQuery      = {}; RGraph.Effects.Fuel             = {}
    RGraph.Effects.Rose             = {}; RGraph.Effects.Odo              = {}
    RGraph.Effects.Gauge            = {}; RGraph.Effects.Meter            = {}
    RGraph.Effects.HBar             = {}; RGraph.Effects.HProgress        = {}
    RGraph.Effects.VProgress        = {}; RGraph.Effects.Radar            = {}
    RGraph.Effects.Waterfall        = {}; RGraph.Effects.Gantt            = {}
    RGraph.Effects.Thermometer      = {}; RGraph.Effects.Scatter          = {}
    RGraph.Effects.Scatter.jQuery   = {}; RGraph.Effects.CornerGauge      = {}
    RGraph.Effects.jQuery.HScissors = {}; RGraph.Effects.jQuery.VScissors = {}



    /**
    * Fadein
    * 
    * This function simply uses the CSS opacity property - initially set to zero and
    * increasing to 1 over the period of 0.5 second
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.Fade.In = function (obj)
    {
        var canvas   = obj.canvas;
        var duration = (arguments[1] && arguments[1].duration ? arguments[1].duration : 250);
        var frames   = (arguments[1] && arguments[1].frames ? arguments[1].frames : 5);

        // Initially the opacity should be zero
        canvas.style.opacity = 0;
        
        // Draw the chart
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        // Now fade the chart in
        for (var i=1; i<=frames; ++i) {
            setTimeout('document.getElementById("' + canvas.id + '").style.opacity = ' + (i * (1 / frames)), i * (duration / frames));
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], duration);
        }
    }


    /**
    * Fadeout
    * 
    * This function is a reversal of the above function - fading out instead of in
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.Fade.Out = function (obj)
    {
        var canvas   = obj.canvas;
        var duration = (arguments[1] && arguments[1].duration ? arguments[1].duration : 250);
        var frames   = (arguments[1] && arguments[1].frames ? arguments[1].frames : 5);
        
        // Draw the chart
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);
        
        // Now fade the chart in
        for (var i=frames; i>=0; --i) {
            setTimeout('document.getElementById("' + canvas.id + '").style.opacity = ' + (i * (1 / frames)), (frames - i) * (duration / frames));
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], duration);
        }
    }


    /**
    * Expand
    * 
    * This effect is like the tooltip effect of the same name. I starts in the middle
    * and expands out to full size.
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.jQuery.Expand = function (obj)
    {
        // Check for jQuery
        if (typeof(jQuery) == 'undefined') {
            alert('[ERROR] Could not find jQuery object - have you included the jQuery file?');
        }
        
        var bounce = (!arguments[1] || (arguments[1] && (arguments[1].bounce || typeof(arguments[1].bounce) == 'undefined'))) ? true : false;

        var canvas = obj.canvas;
        
        if (!canvas.__rgraph_div_placeholder__) {
            var div    = RGraph.Effects.ReplaceCanvasWithDIV(canvas);
            canvas.__rgraph_div_placeholder__ = div;
        } else {
            div = canvas.__rgraph_div_placeholder__;
        }

        div.style.position = 'relative';
        canvas.style.position = 'absolute';
        canvas.style.top  = (canvas.height / 2) + 'px';
        canvas.style.left = (canvas.width / 2) + 'px';
        canvas.style.width  = 0;
        canvas.style.height = 0;


        canvas.style.opacity = 0;


        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        if (bounce) {
            jQuery('#' + obj.id).animate({
                opacity: 1,
                width: '120%',
                height: '120%',
                left: (canvas.width * -0.1) + 'px',
                top: (canvas.height * -0.1) + 'px'
            }, 500, function (){
                        jQuery('#' + obj.id).animate({width: '90%', height: '90%', top: (canvas.height * 0.05) + 'px', left: (canvas.width * 0.05) + 'px'}, 250, function ()
                        {
                            jQuery('#' + obj.id).animate({width: '101%', height: '101%', top: (canvas.height * -0.005) + 'px', left: (canvas.width * -0.005) + 'px'}, 250, function ()
                            {
                                jQuery('#' + obj.id).animate({width: '100%', height: '100%', top: 0, left: 0}, 250);
                            });
                        });
                     });
        } else {
            jQuery('#' + obj.id).animate({
                opacity: 1,
                width: '100%',
                height: '100%',
                left: 0,
                top: 0
            }, 1000)
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], 1000);
        }
    }




    /**
    * Contract
    * 
    * This effect is a good one to use with the Expand effect to make a transition
    * 
    * @param object obj The graph object
    * @param null       Not used
    * @param            Optional callback to run when the effect iss done.
    */
    RGraph.Effects.jQuery.Contract = function (obj)
    {
        // Check for jQuery
        if (typeof(jQuery) == 'undefined') {
            alert('[ERROR] Could not find jQuery object - have you included the jQuery file?');
        }
        
        var canvas = obj.canvas;
        
        if (!canvas.__rgraph_div_placeholder__) {
            var div    = RGraph.Effects.ReplaceCanvasWithDIV(canvas);
            canvas.__rgraph_div_placeholder__ = div;
        } else {
            div = canvas.__rgraph_div_placeholder__;
        }

        div.style.position = 'relative';
        canvas.style.position = 'absolute';
        canvas.style.top      = 0;
        canvas.style.left     = 0;


        jQuery('#' + obj.id).animate({
            width: (canvas.width * 1.2) + 'px',
            height: (canvas.height * 1.2) + 'px',
            left: (canvas.width * -0.1) + 'px',
            top: (canvas.height * -0.1) + 'px'
        }, 250, function ()
        {
                jQuery('#' + obj.id).animate({
                    opacity: 0,
                    width: 0,
                    height: 0,
                    left: (canvas.width * 0.5) + 'px',
                    top: (canvas.height * 0.5) + 'px'
                }, 750)
        });
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], 1000);
        }
    }



    /**
    * A function used to replace the canvas witha Div, which inturn holds the canvas. This way the page
    * layout doesn't shift in the canvas is resized.
    * 
    * @param object canvas The canvas to replace.
    */
    RGraph.Effects.ReplaceCanvasWithDIV  = function (canvas)
    {
        if (!canvas.replacementDIV) {
            // Create the place holder DIV
            var div = document.createElement('DIV');
                div.style.width = canvas.width + 'px';
                div.style.height = canvas.height + 'px';
                div.style.cssFloat = canvas.style.cssFloat;
                div.style.left = canvas.style.left;
                div.style.top = canvas.style.top;
                //div.style.position = canvas.style.position;
                div.style.display = 'inline-block';
            canvas.parentNode.insertBefore(div, canvas);
            
    
            // Remove the canvas from the document
            canvas.parentNode.removeChild(canvas);
            
            // Add it back in as a child of the place holder
            div.appendChild(canvas);
            
            // Reset the positioning information on the canvas
            canvas.style.position = 'relative';
            canvas.style.left = (div.offsetWidth / 2) + 'px';
            canvas.style.top = (div.offsetHeight / 2) + 'px';
            canvas.style.cssFloat = '';
        
            // Add a reference to the canvas to the DIV so that repeated plays of the anumation
            // don't keep replacing the canvas with a new DIV
            canvas.replacementDIV = div;

        } else {
            var div = canvas.replacementDIV;
        }
        
        return div;
    }


    /**
    * Snap
    * 
    * Similar to the tooltip effect of the same name, this moves the canvas in from the top left corner
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.jQuery.Snap = function (obj)
    {
        var delay = 500;

        var div = RGraph.Effects.ReplaceCanvasWithDIV(obj.canvas);
        
        obj.canvas.style.position = 'absolute';
        obj.canvas.style.top = 0;
        obj.canvas.style.left = 0;
        obj.canvas.style.width = 0;
        obj.canvas.style.height = 0;
        obj.canvas.style.opacity = 0;
        
        var targetLeft   = div.offsetLeft;
        var targetTop    = div.offsetTop;
        var targetWidth  = div.offsetWidth;
        var targetHeight = div.offsetHeight;

        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        jQuery('#' + obj.id).animate({
            opacity: 1,
            width: targetWidth + 'px',
            height: targetHeight + 'px',
            left: targetLeft + 'px',
            top: targetTop + 'px'
        }, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay + 50);
        }
    }



    /**
    * Reveal
    * 
    * This effect issmilat to the Expand effect - the canvas is slowly revealed from
    * the centre outwards
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.jQuery.Reveal = function (obj)
    {
        var opts   = arguments[1] ? arguments[1] : null;
        var delay  = 1000;
        var canvas = obj.canvas;
        var xy     = RGraph.getCanvasXY(obj.canvas);


        /**
        * Hide the canvas and draw it
        */
        obj.canvas.style.visibility = 'hidden';
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);


        var divs = [
                    ['reveal_left', xy[0], xy[1], obj.canvas.width  / 2, obj.canvas.height],
                    ['reveal_right',(xy[0] + (obj.canvas.width  / 2)),xy[1],(obj.canvas.width  / 2),obj.canvas.height],
                    ['reveal_top',xy[0],xy[1],obj.canvas.width,(obj.canvas.height / 2)],
                    ['reveal_bottom',xy[0],(xy[1] + (obj.canvas.height  / 2)),obj.canvas.width,(obj.canvas.height / 2)]
                   ];
        
        for (var i=0; i<divs.length; ++i) {
            var div = document.createElement('DIV');
                div.id = divs[i][0];
                div.style.width =  divs[i][3]+ 'px';
                div.style.height = divs[i][4] + 'px';
                div.style.left   = divs[i][1] + 'px';
                div.style.top   = divs[i][2] + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = opts && typeof(opts['color']) == 'string' ? opts['color'] : 'white';
            document.body.appendChild(div);
        }
        
        /**
        * Now the covering DIVs are in place show the canvas again
        */
        obj.canvas.style.visibility = 'visible';


        jQuery('#reveal_left').animate({width: 0}, delay);
        jQuery('#reveal_right').animate({left: '+=' + (obj.canvas.width / 2),width: 0}, delay);
        jQuery('#reveal_top').animate({height: 0}, delay);
        jQuery('#reveal_bottom').animate({top: '+=' + (obj.canvas.height / 2),height: 0}, delay);
        
        // Remove the DIVs from the DOM 100ms after the animation ends
        setTimeout(
            function ()
            {
                document.body.removeChild(document.getElementById("reveal_top"))
                document.body.removeChild(document.getElementById("reveal_bottom"))
                document.body.removeChild(document.getElementById("reveal_left"))
                document.body.removeChild(document.getElementById("reveal_right"))
            }
            , delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }



    /**
    * RevealCircular
    * 
    * This effect is smilar to the Reveal effect - the canvas is slowly revealed from
    * the centre outwards using a circular shape
    * 
    * @param object   obj The chart object
    * @param object       An object of options
    * @param function     An optional callback function that runs when the effect is finished
    */
    RGraph.Effects.RevealCircular = function (obj)
    {
        var opts      = arguments[1] ? arguments[1] : null;
        var callback  = arguments[2] ? arguments[2] : null;
        var frames    = 30;
        var RG        = RGraph;
        var ca        = obj.canvas;
        var co        = obj.context;
        var ra        = 0; // The initial radius of the circle that is clipped to
        var cx        = ca.width / 2;
        var cy        = ca.height / 2;
        var target_ra = Math.max(ca.height, ca.width);
        
        // This is the iterator function which gradually increases the radius of the clip circle
        function Grow ()
        {
            // Begin by clearing the canvas
            RG.Clear(ca);

            co.save();
                // First draw the circle and clip to it
                co.beginPath();
                co.arc(cx, cy, ra, 0, TWOPI, false);
                co.clip();
                
                // Now draw the chart
                obj.Draw();
            co.restore();


            // Increment the radius
            if (ra < target_ra) {
                ra += target_ra / 30;
                RG.Effects.UpdateCanvas(Grow);
            
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }
        
        Grow();
    }



    /**
    * Conceal
    * 
    * This effect is the reverse of the Reveal effect - instead of revealing the canvas it
    * conceals it. Combined with the reveal effect would make for a nice wipe effect.
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.jQuery.Conceal = function (obj)
    {
        var opts   = arguments[1] ? arguments[1] : null;
        var delay  = 1000;
        var canvas = obj.canvas;
        var xy     = RGraph.getCanvasXY(obj.canvas);


        var divs = [
                    ['conceal_left', xy[0], xy[1], 0, obj.canvas.height],
                    ['conceal_right',(xy[0] + obj.canvas.width),xy[1],0,obj.canvas.height],
                    ['conceal_top',xy[0],xy[1],obj.canvas.width,0],
                    ['conceal_bottom',xy[0],(xy[1] + obj.canvas.height),obj.canvas.width,0]
                   ];
        
        for (var i=0; i<divs.length; ++i) {
            var div = document.createElement('DIV');
                div.id = divs[i][0];
                div.style.width =  divs[i][3]+ 'px';
                div.style.height = divs[i][4] + 'px';
                div.style.left   = divs[i][1] + 'px';
                div.style.top   = divs[i][2] + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = opts && typeof(opts['color']) == 'string' ? opts['color'] : 'white';
            document.body.appendChild(div);
        }


        jQuery('#conceal_left').animate({width: '+=' + (obj.canvas.width / 2)}, delay);
        jQuery('#conceal_right').animate({left: '-=' + (obj.canvas.width / 2),width: (obj.canvas.width / 2)}, delay);
        jQuery('#conceal_top').animate({height: '+=' + (obj.canvas.height / 2)}, delay);
        jQuery('#conceal_bottom').animate({top: '-=' + (obj.canvas.height / 2),height: (obj.canvas.height / 2)}, delay);
        
        // Remove the DIVs from the DOM 100ms after the animation ends
        setTimeout(
            function ()
            {
                document.body.removeChild(document.getElementById("conceal_top"))
                document.body.removeChild(document.getElementById("conceal_bottom"))
                document.body.removeChild(document.getElementById("conceal_left"))
                document.body.removeChild(document.getElementById("conceal_right"))
            }
            , delay);
            
        setTimeout(function () {RGraph.Clear(obj.canvas);}, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Horizontal Blinds (open)
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.jQuery.HBlinds.Open = function (obj)
    {
        var canvas  = obj.canvas;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var height = canvas.height / 5;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        for (var i=0; i<5; ++i) {
            var div = document.createElement('DIV');
                div.id = 'blinds_' + i;
                div.style.width =  canvas.width + 'px';
                div.style.height = height + 'px';
                div.style.left   = xy[0] + 'px';
                div.style.top   = (xy[1] + (canvas.height * (i / 5))) + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = color;
            document.body.appendChild(div);

            jQuery('#blinds_' + i).animate({height: 0}, delay);
        }

        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_0'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_1'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_2'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_3'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_4'));}, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Horizontal Blinds (close)
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.jQuery.HBlinds.Close = function (obj)
    {
        var canvas  = obj.canvas;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var height = canvas.height / 5;

        for (var i=0; i<5; ++i) {
            var div = document.createElement('DIV');
                div.id = 'blinds_' + i;
                div.style.width =  canvas.width + 'px';
                div.style.height = 0;
                div.style.left   = xy[0] + 'px';
                div.style.top   = (xy[1] + (canvas.height * (i / 5))) + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = color;
            document.body.appendChild(div);

            jQuery('#blinds_' + i).animate({height: height + 'px'}, delay);
        }
        
        setTimeout(function () {RGraph.Clear(obj.canvas);}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_0'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_1'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_2'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_3'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_4'));}, delay + 100);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Vertical Blinds (open)
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.jQuery.VBlinds.Open = function (obj)
    {
        var canvas  = obj.canvas;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var width  = canvas.width / 10;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        for (var i=0; i<10; ++i) {
            var div = document.createElement('DIV');
                div.id = 'blinds_' + i;
                div.style.width =  width + 'px';
                div.style.height = canvas.height + 'px';
                div.style.left   = (xy[0] + (canvas.width * (i / 10))) + 'px';
                div.style.top   = (xy[1]) + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = color;
            document.body.appendChild(div);

            jQuery('#blinds_' + i).animate({width: 0}, delay);
        }

        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_0'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_1'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_2'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_3'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_4'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_5'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_6'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_7'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_8'));}, delay + 100);
        setTimeout(function () {document.body.removeChild(document.getElementById('blinds_9'));}, delay + 100);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Vertical Blinds (close)
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.jQuery.VBlinds.Close = function (obj)
    {
        var canvas  = obj.canvas;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var width  = canvas.width / 10;
        
        // Don't draw the chart

        for (var i=0; i<10; ++i) {
            var div = document.createElement('DIV');
                div.id = 'blinds_' + i;
                div.style.width =  0;
                div.style.height = canvas.height + 'px';
                div.style.left   = (xy[0] + (canvas.width * (i / 10))) + 'px';
                div.style.top   = (xy[1]) + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = color;
            document.body.appendChild(div);

            jQuery('#blinds_' + i).animate({width: width}, delay);
        }

        setTimeout(function () {RGraph.Clear(obj.canvas, color);}, delay + 100);

        if (opts['remove']) {
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_0'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_1'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_2'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_3'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_4'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_5'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_6'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_7'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_8'));}, delay + 100);
            setTimeout(function () {document.body.removeChild(document.getElementById('blinds_9'));}, delay + 100);
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Pie chart grow
    * 
    * Gradually increases the pie chart radius
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.Pie.Grow = function (obj)
    {
        var canvas  = obj.canvas;
        var opts   = arguments[1] ? arguments[1] : [];
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        
        canvas.style.visibility = 'hidden';
        RGraph.RedrawCanvas(canvas);

        var radius = obj.getRadius();
        
        if (typeof(obj.Get('chart.radius')) == 'number') {
            radius = obj.Get('chart.radius');
        }
        
        //RGraph.Clear(obj.canvas);
        canvas.style.visibility = 'visible';

        obj.Set('chart.radius', 0);

        RGraph.Effects.Animate(obj, {'chart.radius': radius}, arguments[2]);
    }


    /**
    * Grow
    * 
    * The Bar chart Grow effect gradually increases the values of the bars
    * 
    * @param object   obj The graph object
    * @param object       An array of options
    * @param function     A function to call when the effect is complete
    */
    RGraph.Effects.Bar.Grow = function (obj)
    {
        // Callback
        var callback = arguments[2];

        // Save the data
        obj.original_data = RGraph.array_clone(obj.data);
        
        // Zero the data
        obj.__animation_frame__ = 0;

        // Stop the scale from changing by setting chart.ymax (if it's not already set)
        if (obj.Get('chart.ymax') == null) {

            var ymax = 0;

            for (var i=0; i<obj.data.length; ++i) {
                if (RGraph.is_array(obj.data[i]) && obj.Get('chart.grouping') == 'stacked') {
                    ymax = Math.max(ymax, Math.abs(RGraph.array_sum(obj.data[i])));

                } else if (RGraph.is_array(obj.data[i]) && obj.Get('chart.grouping') == 'grouped') {
                    ymax = Math.max(ymax, Math.abs(RGraph.array_max(obj.data[i])));
                } else {
                    ymax = Math.max(ymax, Math.abs(obj.data[i]));
                }
            }

            var scale = RGraph.getScale2(obj, {'max':ymax});
            obj.Set('chart.ymax', scale.max);
        }

        function Grow ()
        {
            var numFrames = 30;

            if (!obj.__animation_frame__) {
                obj.__animation_frame__  = 0;
                obj.__original_hmargin__ = obj.Get('chart.hmargin');
                obj.__hmargin__          = ((obj.canvas.width - obj.Get('chart.gutter.left') - obj.Get('chart.gutter.right')) / obj.data.length) / 2;
                obj.Set('chart.hmargin', obj.__hmargin__);
            }

            // Alter the Bar chart data depending on the frame
            for (var j=0; j<obj.original_data.length; ++j) {
                if (typeof(obj.data[j]) == 'object') {
                    for (var k=0; k<obj.data[j].length; ++k) {
                        obj.data[j][k] = (obj.__animation_frame__ / numFrames) * obj.original_data[j][k];
                    }
                } else {
                    obj.data[j] = (obj.__animation_frame__ / numFrames) * obj.original_data[j];
                }
            }

            /**
            * Increment the hmargin to the target
            */
            obj.Set('chart.hmargin', ((1 - (obj.__animation_frame__ / numFrames)) * (obj.__hmargin__ - obj.__original_hmargin__)) + obj.__original_hmargin__);


            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (obj.__animation_frame__ < numFrames) {
                obj.__animation_frame__ += 1;

                RGraph.Effects.UpdateCanvas(Grow);
            // Call the callback function if it's defined
            } else {
                if (callback) {
                    callback(obj);
                }
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow);
    }


    /**
    * A wrapper function that encapsulate requestAnimationFrame
    * 
    * @param function func The animation function
    */
    RGraph.Effects.UpdateCanvas = function (func)
    {
        window.requestAnimationFrame =    window.requestAnimationFrame
                                       || window.webkitRequestAnimationFrame
                                       || window.msRequestAnimationFrame
                                       || window.amozRequestAnimationFrame
                                       || (function (func){setTimeout(func, 16.666);});
        
        window.requestAnimationFrame(func);
    }


    /**
    * Grow
    * 
    * The Fuel chart Grow effect gradually increases the values of the Fuel chart
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.Fuel.Grow = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = 30;
        var frame     = 0;
        var origValue = Number(obj.currentValue);
        
        if (obj.currentValue == null) {
            obj.currentValue = obj.min;
            origValue = obj.min;
        }

        var newValue  = obj.value;
        var diff      = newValue - origValue;
        var step      = (diff / numFrames);
        var frame     = 0;


        function Grow ()
        {
            frame++;

            obj.value = ((frame / numFrames) * diff) + origValue

            if (obj.value > obj.max) obj.value = obj.max;
            if (obj.value < obj.min) obj.value = obj.min;

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (frame < numFrames) {
                RGraph.Effects.UpdateCanvas(Grow);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }

        Grow();
    }


    /**
    * The Animate function. Similar to the jQuery Animate() function - simply pass it a
    * map of the properties and their target values, and this function will animate
    * them to get to those values.
    * 
    * @param object obj The chart object
    * @param object map A map (an associative array) of the properties and their target values.
    * @param            An optional function which will be called when the animation is complete
    */
    RGraph.Effects.Animate = function (obj, map)
    {
        RGraph.RedrawCanvas(obj.canvas);

        RGraph.Effects.__total_frames__  = (map && map['frames']) ? map['frames'] : 30;

        function Animate_Iterator (func)
        {
            var id = [obj.id +  '_' + obj.type];

            // Very first time in - initialise the arrays
            if (typeof(RGraph.Effects.__current_frame__ ) == 'undefined') {
                RGraph.Effects.__current_frame__   = new Array();
                RGraph.Effects.__original_values__ = new Array();
                RGraph.Effects.__diffs__           = new Array();
                RGraph.Effects.__steps__           = new Array();
                RGraph.Effects.__callback__        = new Array();
            }

            // Initialise the arrays for THIS animation (not necessrily the first in the page)
            if (!RGraph.Effects.__current_frame__[id]) {
                RGraph.Effects.__current_frame__[id] = RGraph.Effects.__total_frames__;
                RGraph.Effects.__original_values__[id] = {};
                RGraph.Effects.__diffs__[id]           = {};
                RGraph.Effects.__steps__[id]           = {};
                RGraph.Effects.__callback__[id]        = func;
            }

            for (var i in map) {
                if (typeof(map[i]) == 'string' || typeof(map[i]) == 'number') {

                    // If this the first frame, record the proginal value
                    if (RGraph.Effects.__current_frame__[id] == RGraph.Effects.__total_frames__) {
                        RGraph.Effects.__original_values__[id][i] = obj.Get(i);
                        RGraph.Effects.__diffs__[id][i]           = map[i] - RGraph.Effects.__original_values__[id][i];
                        RGraph.Effects.__steps__[id][i]           = RGraph.Effects.__diffs__[id][i] / RGraph.Effects.__total_frames__;
                    }

                    obj.Set(i, obj.Get(i) + RGraph.Effects.__steps__[id][i]);

                    RGraph.RedrawCanvas(obj.canvas);
                }
            }

            // If the current frame number is above zero, run the animation iterator again
            if (--RGraph.Effects.__current_frame__[id] > 0) {
                //setTimeout(Animate_Iterator, 100)
                RGraph.Effects.UpdateCanvas(Animate_Iterator);
            
            // Optional callback
            } else {

                if (typeof(RGraph.Effects.__callback__[id]) == 'function') {
                    (RGraph.Effects.__callback__[id])(obj);
                }
                
                // Get rid of the arrays
                RGraph.Effects.__current_frame__[id]   = null;
                RGraph.Effects.__original_values__[id] = null;
                RGraph.Effects.__diffs__[id]           = null;
                RGraph.Effects.__steps__[id]           = null;
                RGraph.Effects.__callback__[id]        = null;

            }
        }

        Animate_Iterator(arguments[2]);
    }


    /**
    * Slide in
    * 
    * This function is a wipe that can be used when switching the canvas to a new graph
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.jQuery.Slide.In = function (obj)
    {
        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        var canvas = obj.canvas;
        var div    = RGraph.Effects.ReplaceCanvasWithDIV(obj.canvas);
        var delay = 1000;
        div.style.overflow= 'hidden';
        var from = typeof(arguments[1]) == 'object' && typeof(arguments[1]['from']) == 'string' ? arguments[1]['from'] : 'left';
        
        canvas.style.position = 'relative';
        
        if (from == 'left') {
            canvas.style.left = (0 - div.offsetWidth) + 'px';
            canvas.style.top  = 0;
        } else if (from == 'top') {
            canvas.style.left = 0;
            canvas.style.top  = (0 - div.offsetHeight) + 'px';
        } else if (from == 'bottom') {
            canvas.style.left = 0;
            canvas.style.top  = div.offsetHeight + 'px';
        } else {
            canvas.style.left = div.offsetWidth + 'px';
            canvas.style.top  = 0;
        }
        
        jQuery('#' + obj.id).animate({left:0,top:0}, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Slide out
    * 
    * This function is a wipe that can be used when switching the canvas to a new graph
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.jQuery.Slide.Out = function (obj)
    {
        var canvas = obj.canvas;
        var div    = RGraph.Effects.ReplaceCanvasWithDIV(obj.canvas);
        var delay = 1000;
        div.style.overflow= 'hidden';
        var to = typeof(arguments[1]) == 'object' && arguments[1] && typeof(arguments[1]['to']) == 'string' ? arguments[1]['to'] : 'left';
        
        canvas.style.position = 'relative';
        canvas.style.left = 0;
        canvas.style.top  = 0;
        
        if (to == 'left') {
            jQuery('#' + obj.id).animate({left: (0 - canvas.width) + 'px'}, delay);
        } else if (to == 'top') {
            jQuery('#' + obj.id).animate({left: 0, top: (0 - div.offsetHeight) + 'px'}, delay);
        } else if (to == 'bottom') {
            jQuery('#' + obj.id).animate({top: (0 + div.offsetHeight) + 'px'}, delay);
        } else {
            jQuery('#' + obj.id).animate({left: (0 + canvas.width) + 'px'}, delay);
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Unfold
    * 
    * This effect gradually increases the X/Y coordinatesfrom 0
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.Line.Unfold = function (obj)
    {
        obj.Set('chart.animation.factor', obj.Get('chart.animation.unfold.initial'));
        RGraph.Effects.Animate(obj, {'chart.animation.factor': 1}, arguments[2]);
    }


    /**
    * RoundRobin
    * 
    * This effect is similar to the Pie chart RoundRobin effect
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Rose.RoundRobin = function (obj)
    {
        var numFrames       = 60;
        var currentFrame    = 0;
        var original_margin = obj.Get('chart.margin');
        var margin          = (360 / obj.data.length) / 2;
        var callback        = arguments[2];

        obj.Set('chart.margin', margin);
        obj.Set('chart.animation.roundrobin.factor', 0);

        //RGraph.Effects.Animate(obj, {'chart.margin': original_margin, 'chart.animation.grow.factor': 1, 'frames': 45}, arguments[2]);
        function RoundRobin_inner ()
        {
            if (currentFrame++ < numFrames) {
                obj.Set('chart.animation.roundrobin.factor', currentFrame / numFrames);
                obj.Set('chart.margin', (currentFrame / numFrames) * original_margin);
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                RGraph.Effects.UpdateCanvas(RoundRobin_inner);

            } else {
                obj.Set('chart.animation.roundrobin.factor', 1);
                obj.Set('chart.margin', original_margin);
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
        }
        
        RGraph.Effects.UpdateCanvas(RoundRobin_inner);
    }


    /**
    * UnfoldFromCenter
    * 
    * Line chart  unfold from center
    */
    RGraph.Effects.Line.UnfoldFromCenter = function (obj)
    {
        var numFrames = 30;

        var original_opacity = obj.canvas.style.opacity;
        obj.canvas.style.opacity = 0;
        
        obj.Draw();
        RGraph.RedrawCanvas(obj.canvas);

        var center_value = obj.Get('chart.xaxispos') == 'center' ? obj.Get('chart.ymin') : ((obj.max - obj.min) / 2) + obj.min;
        obj.Set('chart.ymax', obj.scale2.max);

        RGraph.Clear(obj.canvas);

        obj.canvas.style.opacity = original_opacity;
        var original_data = RGraph.array_clone(obj.original_data);
        var callback = arguments[2];

        if (!obj.__increments__) {
        
            obj.__increments__ = new Array();
        
            for (var dataset=0; dataset<original_data.length; ++dataset) {

                obj.__increments__[dataset] = new Array();

                for (var i=0; i<original_data[dataset].length; ++i) {
                    if (obj.Get('chart.filled') && obj.Get('chart.filled.accumulative') && dataset > 0) {
                        obj.__increments__[dataset][i] = original_data[dataset][i] / numFrames;
                        obj.original_data[dataset][i] = 0;
                    } else {
                        obj.__increments__[dataset][i] = (original_data[dataset][i] - center_value) / numFrames;
                        obj.original_data[dataset][i] = center_value;
                    }
                }
            }
        }

        function UnfoldFromCenter ()
        {
            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);
        
            for (var dataset=0; dataset<original_data.length; ++dataset) {
                for (var i=0; i<original_data[dataset].length; ++i) {
                    obj.original_data[dataset][i] += obj.__increments__[dataset][i];
                }
            }

            if (--numFrames > 0) {
                RGraph.Effects.UpdateCanvas(UnfoldFromCenter);
            } else {
                obj.original_data = RGraph.array_clone(original_data);
                obj.__increments__ = null;
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
        }
        
        UnfoldFromCenter();
    }



    /**
    * UnfoldFromCenterTrace
    */
    RGraph.Effects.Line.jQuery.UnfoldFromCenterTrace = function  (obj)
    {
        // Hide the canvas first
        obj.canvas.style.visibility = 'hidden';
        setTimeout(function () {obj.canvas.style.visibility = 'visible';}, 10);

        /**
        * First draw the chart so we can get the max
        */
        obj.Draw();
        RGraph.Clear(obj.canvas);


        var data = RGraph.array_clone(obj.original_data);
        var callback = arguments[2];

        /**
        * When the Trace function finishes it calls this function
        */
        function Unfold_callback ()
        {
            obj.original_data = data;
            RGraph.Effects.Line.UnfoldFromCenter(obj, null, callback);
        }

        /**
        * Determine the mid-point
        */
        var half = obj.Get('chart.xaxispos') == 'center' ? obj.min : ((obj.max - obj.min) / 2) + obj.min;
        obj.Set('chart.ymax', obj.max);

        for (var i=0; i<obj.original_data.length; ++i) {
            for (var j=0; j<obj.original_data[i].length; ++j) {
                obj.original_data[i][j] = (obj.Get('chart.filled') && obj.Get('chart.filled.accumulative') && i > 0) ? 0 : half;
            }
        }

        //RGraph.Clear(obj.canvas);
        RGraph.Effects.Line.jQuery.Trace(obj, {'duration':1000}, Unfold_callback);
    }



    /**
    * FoldToCenter
    * 
    * Line chart  FoldTocenter
    */
    RGraph.Effects.Line.FoldToCenter = function (obj)
    {
        var totalFrames = 30;
        var numFrame    = totalFrames;
        RGraph.RedrawCanvas(obj.canvas);
        var center_value = obj.scale2.max / 2;
        obj.Set('chart.ymax', obj.scale2.max);
        RGraph.Clear(obj.canvas);
        var original_data = RGraph.array_clone(obj.original_data);
        obj.Set('chart.shadow.blur', 0);
        var callback = arguments[2];
        
        function FoldToCenter ()
        {
            for (var i=0; i<obj.data.length; ++i) {
                if (obj.data[i].length) {
                    for (var j=0; j<obj.data[i].length; ++j) {
                        if (obj.original_data[i][j] > center_value) {
                            obj.original_data[i][j] = ((original_data[i][j] - center_value) * (numFrame/totalFrames)) + center_value;
                        } else {
                            obj.original_data[i][j] = center_value - ((center_value - original_data[i][j]) * (numFrame/totalFrames));
                        }
                    }
                }
            }
            
            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas)

            if (numFrame-- > 0) {
                RGraph.Effects.UpdateCanvas(FoldToCenter);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }

        RGraph.Effects.UpdateCanvas(FoldToCenter);
    }


    /**
    * Odo Grow
    * 
    * This effect gradually increases the represented value
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Odo.Grow = function (obj)
    {
        var numFrames = 30;
        var curFrame  = 0;
        var origValue = Number(obj.currentValue);
        var newValue  = obj.value;
        var diff      = newValue - origValue;
        var step      = (diff / numFrames);
        var callback  = arguments[2];

        function Grow_inner ()
        {
            obj.value = origValue + (curFrame * step);

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (++curFrame <= numFrames) {
                RGraph.Effects.UpdateCanvas(Grow_inner);
            } else if (callback) {
                callback(obj);
            }
        }
        
        //setTimeout(Grow, 100);
        Grow_inner();
    }


    /**
    * Meter Grow
    * 
    * This effect gradually increases the represented value
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Meter.Grow = function (obj)
    {
        if (!obj.currentValue) {
            obj.currentValue = obj.min;
        }

        var totalFrames = 60;
        var numFrame    = 0;
        var diff        = obj.value - obj.currentValue;
        var step        = diff / totalFrames
        var callback    = arguments[2];
        var initial     = obj.currentValue;

        function Grow_meter_inner ()
        {
            obj.value = initial + (numFrame++ * step);

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);
        
            if (numFrame++ <= totalFrames) {
                RGraph.Effects.UpdateCanvas(Grow_meter_inner);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }
        
        Grow_meter_inner();
    }


    /**
    * Grow
    * 
    * The HBar chart Grow effect gradually increases the values of the bars
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.HBar.Grow = function (obj)
    {
        // Save the data
        obj.original_data = RGraph.array_clone(obj.data);
        
        // Zero the data
        obj.__animation_frame__ = 0;

        // Stop the scale from changing by setting chart.ymax (if it's not already set)
        if (obj.Get('chart.xmax') == 0) {

            var xmax = 0;

            for (var i=0; i<obj.data.length; ++i) {
                if (RGraph.is_array(obj.data[i]) && obj.Get('chart.grouping') == 'stacked') {
                    xmax = Math.max(xmax, RGraph.array_sum(obj.data[i]));
                } else if (RGraph.is_array(obj.data[i]) && obj.Get('chart.grouping') == 'grouped') {
                    xmax = Math.max(xmax, RGraph.array_max(obj.data[i]));
                } else {
                    xmax = Math.max(xmax, RGraph.array_max(obj.data[i]));
                }
            }

            var scale2 = RGraph.getScale2(obj, {'max':xmax});
            obj.Set('chart.xmax', scale2.max);
        }
        
        /**
        * Turn off shadow blur for the duration of the animation
        */
        if (obj.Get('chart.shadow.blur') > 0) {
            var __original_shadow_blur__ = obj.Get('chart.shadow.blur');
            obj.Set('chart.shadow.blur', 0);
        }

        function Grow ()
        {
            var numFrames = 30;

            if (!obj.__animation_frame__) {
                obj.__animation_frame__  = 0;
                obj.__original_vmargin__ = obj.Get('chart.vmargin');
                obj.__vmargin__          = ((obj.canvas.height - obj.Get('chart.gutter.top') - obj.Get('chart.gutter.bottom')) / obj.data.length) / 2;
                obj.Set('chart.vmargin', obj.__vmargin__);
            }

            // Alter the Bar chart data depending on the frame
            for (var j=0; j<obj.original_data.length; ++j) {
                
                // This stops the animatioon from being completely linear
                var easing = Math.pow(Math.sin((obj.__animation_frame__ * (90 / numFrames)) / (180 / PI)), 4);

                if (typeof(obj.data[j]) == 'object') {
                    for (var k=0; k<obj.data[j].length; ++k) {
                        obj.data[j][k] = (obj.__animation_frame__ / numFrames) * obj.original_data[j][k] * easing;
                    }
                } else {
                    obj.data[j] = (obj.__animation_frame__ / numFrames) * obj.original_data[j] * easing;
                }
            }

            /**
            * Increment the vmargin to the target
            */
            obj.Set('chart.vmargin', ((1 - (obj.__animation_frame__ / numFrames)) * (obj.__vmargin__ - obj.__original_vmargin__)) + obj.__original_vmargin__);


            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (obj.__animation_frame__ < numFrames) {
                obj.__animation_frame__ += 1;
                
                RGraph.Effects.UpdateCanvas(Grow);
            
            // Turn any shadow blur back on
            } else {
                if (typeof(__original_shadow_blur__) == 'number' && __original_shadow_blur__ > 0) {
                    obj.Set('chart.shadow.blur', __original_shadow_blur__);
                    RGraph.Clear(obj.canvas);
                    RGraph.RedrawCanvas(obj.canvas);
                }
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow);
    }


    /**
    * Trace
    * 
    * This effect is for the Line chart, uses the jQuery library and slowly
    * uncovers the Line , but you can see the background of the chart. This effect
    * is quite new (1/10/2011) and as such should be used with caution.
    * 
    * @param object obj The graph object
    * @param object     Not used
    * @param int        A number denoting how long (in millseconds) the animation should last for. Defauld
    *                   is 1500
    */
    RGraph.Effects.Line.jQuery.Trace = function (obj)
    {
        var callback = typeof(arguments[2]) == 'function' ? arguments[2] : function () {};
        var opt = arguments[1] || [];
        
        if (!opt['duration']) {
            opt['duration'] = 1000;
        }

        RGraph.Clear(obj.canvas);
        //obj.Draw();
        RGraph.RedrawCanvas(obj.canvas);

        /**
        * Create the DIV that the second canvas will sit in
        */
        var div = document.createElement('DIV');
            var xy = RGraph.getCanvasXY(obj.canvas);
            div.id = '__rgraph_trace_animation_' + RGraph.random(0, 4351623) + '__';
            div.style.left = xy[0] + 'px';
            div.style.top = xy[1] + 'px';
            div.style.width = obj.Get('chart.gutter.left');
            div.style.height = obj.canvas.height + 'px';
            div.style.position = 'absolute';
            div.style.overflow = 'hidden';
        document.body.appendChild(div);
        
        obj.canvas.__rgraph_trace_div__ = div;

        /**
        * Make the second canvas
        */
        var id      = '__rgraph_line_trace_animation_' + RGraph.random(0, 99999999) + '__';
        var canvas2 = document.createElement('CANVAS');




        // Copy the 3D CSS transformation properties across from the original canvas
        var properties = ['WebkitTransform','MozTransform','OTransform','MSTransform','transform'];
        
        for (i in properties) {
            var name = properties[i];
            if (typeof(obj.canvas.style[name]) == 'string' && obj.canvas.style[name]) {
                canvas2.style[name] = obj.canvas.style[name];
            }
        }
        
        

        obj.canvas.__rgraph_line_canvas2__ = canvas2;
        canvas2.width = obj.canvas.width;
        canvas2.height = obj.canvas.height;
        canvas2.style.position = 'absolute';
        canvas2.style.left = 0;
        canvas2.style.top  = 0;


        // This stops the clear effect clearing the canvas - which can happen if you have multiple canvas tags on the page all with
        // dynamic effects that do redrawing
        canvas2.noclear = true;

        canvas2.id         = id;
        div.appendChild(canvas2);

        var reposition_canvas2 = function (e)
        {
            var xy = RGraph.getCanvasXY(obj.canvas);
            
            div.style.left = xy[0] + 'px';
            div.style.top = xy[1] + 'px';
        }
        window.addEventListener('resize', reposition_canvas2, false)
        
        /**
        * Make a copy of the original Line object
        */
        var obj2 = new RGraph.Line(id, RGraph.array_clone(obj.original_data));
        
        // Remove the new line from the ObjectRegistry so that it isn't redawn
        RGraph.ObjectRegistry.Remove(obj2);

        for (i in obj.properties) {
            if (typeof(i) == 'string') {
                obj2.Set(i, obj.properties[i]);
            }
        }

        //obj2.Set('chart.tooltips', null);
        obj2.Set('chart.labels', []);
        obj2.Set('chart.background.grid', false);
        obj2.Set('chart.background.barcolor1', 'rgba(0,0,0,0)');
        obj2.Set('chart.background.barcolor2', 'rgba(0,0,0,0)');
        obj2.Set('chart.ylabels', false);
        obj2.Set('chart.noaxes', true);
        obj2.Set('chart.title', '');
        obj2.Set('chart.title.xaxis', '');
        obj2.Set('chart.title.yaxis', '');
        obj2.Set('chart.filled.accumulative', obj.Get('chart.filled.accumulative'));
        obj.Set('chart.key', []);
        obj2.Draw();
        
        obj.canvas.__rgraph_trace_obj2__ = obj2;


        /**
        * This effectively hides the line
        */
        obj.Set('chart.line.visible', false);
        obj.Set('chart.colors', ['rgba(0,0,0,0)']);
        if (obj.Get('chart.filled')) {
            var original_fillstyle = obj.Get('chart.fillstyle');
            obj.Set('chart.fillstyle', 'rgba(0,0,0,0)');
            obj.Set('chart.animation.trace.original.fillstyle', original_fillstyle);
        }

        RGraph.Clear(obj.canvas);
        //obj.Draw();
        RGraph.RedrawCanvas(obj.canvas);
        
        /**
        * Place a DIV over the canvas to stop interaction with it
        */
        if (!obj.canvas.__rgraph_trace_cover__) {
            var div2 = document.createElement('DIV');
                div2.id = '__rgraph_trace_animation_' + RGraph.random(0, 4351623) + '__';
                div2.style.left = xy[0] + 'px';
                div2.style.top = xy[1] + 'px';
                div2.style.width = obj.canvas.width + 'px';
                div2.style.height = obj.canvas.height + 'px';
                div2.style.position = 'absolute';
                div2.style.overflow = 'hidden';
                div2.style.backgroundColor = 'rgba(0,0,0,0)';
                div.div2 = div2;
                obj.canvas.__rgraph_trace_cover__ = div2;
            document.body.appendChild(div2);
        } else {
            div2 = obj.canvas.__rgraph_trace_cover__;
        }

        /**
        * Animate the DIV that contains the canvas
        */
        jQuery('#' + div.id).animate({
            width: obj.canvas.width + 'px'
        }, opt['duration'], function () {RGraph.Effects.Line.Trace_callback(obj)});


        /**
        * Get rid of the second canvas and turn the line back on
        * on the original.
        */
        RGraph.Effects.Line.Trace_callback = function (obj)
        {
            var obj2 = obj.canvas.__rgraph_trace_obj2__;

            // Remove the window resize listener
            window.removeEventListener('resize', reposition_canvas2, false);

            div.style.display = 'none';
            div2.style.display = 'none';

            //div.removeChild(canvas2);
            obj.Set('chart.line.visible', true);
            
            // Revert the filled status back to as it was
            obj.Set('chart.filled', RGraph.array_clone(obj2.Get('chart.filled')));
            obj.Set('chart.fillstyle', obj.Get('chart.animation.trace.original.fillstyle'));
            obj.Set('chart.colors', RGraph.array_clone(obj2.Get('chart.colors')));
            obj.Set('chart.key', RGraph.array_clone(obj2.Get('chart.key')));

            RGraph.RedrawCanvas(obj.canvas);

            obj.canvas.__rgraph_trace_div__.style.display = 'none';
            obj.canvas.__rgraph_trace_div__ = null;
            obj.canvas.__rgraph_line_canvas2__.style.display = 'none';
            obj.canvas.__rgraph_line_canvas2__ = null;
            obj.canvas.__rgraph_trace_cover__.style.display = 'none';
            obj.canvas.__rgraph_trace_cover__ = null;
            
            
            callback(obj);
        }
    }



    /**
    * Trace2
    * 
    * This is a new version of the Trace effect which no longer requires jQuery and is more compatible
    * with other effects (eg Expand). This new effect is considerably simpler and less code.
    * 
    * @param object obj The graph object
    * @param object     Options for the effect. Currently only "frames" is available.
    * @param int        A function that is called when the ffect is complete
    */
    RGraph.Effects.Line.Trace2 = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = (arguments[1] && arguments[1].frames) ? arguments[1].frames : 60;
        var frame     = 0;
        
        obj.Set('animation.trace.clip', 0);

        function Grow ()
        {
            if (frame > numFrames) {
                if (callback) {
                    callback(obj);
                }
                return;
            }

            obj.Set('animation.trace.clip', frame / numFrames );

            RGraph.RedrawCanvas(obj.canvas);

            frame++;
            RGraph.Effects.UpdateCanvas(Grow);
        }
        
        Grow();
    }



    /**
    * Trace (Radar chart)
    * 
    * This is a Trace effect for the Radar chart
    * 
    * @param object obj The graph object
    * @param object     Options for the effect. Currently only "frames" is available.
    * @param function   A function that is called when the ffect is complete
    */
    RGraph.Effects.Radar.Trace = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = (arguments[1] && arguments[1].frames) ? arguments[1].frames : 60;
        var frame     = 0;

        obj.Set('animation.trace.clip', 0);

        function Grow ()
        {
            if (frame > numFrames) {
                if (callback) {
                    callback(obj);
                }
                return;
            }

            obj.Set('animation.trace.clip', frame / numFrames );

            RGraph.RedrawCanvas(obj.canvas);

            frame++;
            RGraph.Effects.UpdateCanvas(Grow);
        }
        
        Grow();
    }



    /**
    * RoundRobin
    * 
    * This effect does two things:
    *  1. Gradually increases the size of each segment
    *  2. Gradually increases the size of the radius from 0
    * 
    * @param object obj The graph object
    */
    RGraph.Effects.Pie.RoundRobin = function (obj)
    {
        var callback     = arguments[2] ? arguments[2] : null;
        var opt          = arguments[1];
        var currentFrame = 0;
        var numFrames    = (opt && opt['frames']) ? opt['frames'] : 90;
        var targetRadius =  obj.getRadius();
        
        obj.Set('chart.events', false);
        
        // Fix for donuts
        if (obj.properties['chart.variant'] == 'donut' && typeof(obj.properties['chart.variant.donut.width']) == 'number') {
            if (RGraph.is_null(opt)) {
                var opt = {radius: null}
            } else {
                opt.radius = null;
            }
        }


        function RoundRobin_inner ()
        {
            obj.Set('chart.effect.roundrobin.multiplier', Math.pow(Math.sin((currentFrame * (90 / numFrames)) / (180 / PI)), 2) * (currentFrame / numFrames) );

            if (!opt || typeof(opt['radius']) == 'undefined' || opt['radius'] == true) {
                obj.Set('chart.radius', targetRadius * obj.Get('chart.effect.roundrobin.multiplier'));
            }
            
            RGraph.RedrawCanvas(obj.canvas);

            if (currentFrame++ < numFrames) {
                RGraph.Effects.UpdateCanvas(RoundRobin_inner);
            
            } else {
                
                // Re-enable the events and redraw the chart.
                obj.Set('chart.events', true);
                RGraph.RedrawCanvas(obj.canvas);

                if (callback) {
                    callback(obj);
                }
            }
        }

        RGraph.Effects.UpdateCanvas(RoundRobin_inner);
    }


    /**
    * Implode (pie chart)
    * 
    * Here the segments are initially exploded - and gradually
    * contract inwards to create the Pie chart
    * 
    * @param object obj The Pie chart object
    */
    RGraph.Effects.Pie.Implode = function (obj)
    {
        var numFrames = 90;
        var distance  = Math.min(obj.canvas.width, obj.canvas.height);
        var exploded  = obj.Get('chart.exploded');
        var callback  = arguments[2];
        
        function Implode_inner ()
        {
            obj.Set('chart.exploded', Math.sin(numFrames / (180 / PI)) * distance);
            RGraph.Clear(obj.canvas)
            //obj.Draw();
            RGraph.RedrawCanvas(obj.canvas);

            if (numFrames > 0) {
                numFrames--;
                RGraph.Effects.UpdateCanvas(Implode_inner);
            } else {
                // Finish off the animation
                obj.Set('chart.exploded', exploded);
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
        }
        
        RGraph.Effects.UpdateCanvas(Implode_inner);
    }



    /**
    * Pie chart explode
    * 
    * Explodes the Pie chart - gradually incrementing the size of the chart.explode property
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.Pie.Explode = function (obj)
    {
        var canvas   = obj.canvas;
        var opts     = arguments[1] ? arguments[1] : [];
        var callback = arguments[2] ? arguments[2] : null;
        var frames   = opts['frames'] ? opts['frames'] : 60;

        obj.Set('chart.exploded', 0);

        RGraph.Effects.Animate(obj, {'frames': frames, 'chart.exploded': Math.max(canvas.width, canvas.height)}, callback);
    }



    /**
    * Gauge Grow
    * 
    * This effect gradually increases the represented value
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Gauge.Grow = function (obj)
    {

        var callback  = arguments[2];
        var numFrames = 30;
        var frame     = 0;

        // Single pointer
        if (typeof(obj.value) == 'number') {

            var origValue = Number(obj.currentValue);

            if (obj.currentValue == null) {
                obj.currentValue = obj.min;
                origValue = obj.min;
            }

            var newValue  = obj.value;
            var diff      = newValue - origValue;
            var step      = (diff / numFrames);
            var frame     = 0;


            function Grow_single ()
            {

                frame++;

                obj.value = ((frame / numFrames) * diff) + origValue;

                if (obj.value > obj.max) obj.value = obj.max;
                if (obj.value < obj.min) obj.value = obj.min;
    
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
    
                if (frame < 30) {
                    RGraph.Effects.UpdateCanvas(Grow_single);
                } else if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }

            Grow_single();

        // Multiple pointers
        } else {

            if (obj.currentValue == null) {
                obj.currentValue = [];
                
                for (var i=0; i<obj.value.length; ++i) {
                    obj.currentValue[i] = obj.min;
                }
                
                origValue = RGraph.array_clone(obj.currentValue);
            }

            var origValue = RGraph.array_clone(obj.currentValue);
            var newValue  = RGraph.array_clone(obj.value);
            var diff      = [];
            var step      = [];
            
            for (var i=0; i<newValue.length; ++i) {
                diff[i] = newValue[i] - Number(obj.currentValue[i]);
                step[i] = (diff[i] / numFrames);
            }



            function Grow_multiple ()
            {
                frame++;
                
                for (var i=0; i<obj.value.length; ++i) {
                    
                    obj.value[i] = ((frame / numFrames) * diff[i]) + origValue[i];

                    if (obj.value[i] > obj.max) obj.value[i] = obj.max;
                    if (obj.value[i] < obj.min) obj.value[i] = obj.min;
        
                    RGraph.Clear(obj.canvas);
                    RGraph.RedrawCanvas(obj.canvas);
                    
                }
    
                if (frame < 30) {
                    RGraph.Effects.UpdateCanvas(Grow_multiple);
                } else if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
    
            Grow_multiple();
        }
    }


    /**
    * Radar chart grow
    * 
    * This effect gradually increases the magnitude of the points on the radar chart
    * 
    * @param object obj The chart object
    * @param null       Not used
    * @param function   An optional callback that is run when the effect is finished
    */
    RGraph.Effects.Radar.Grow = function (obj)
    {
        var totalframes   = 30;
        var framenum      = totalframes;
        var data          = RGraph.array_clone(obj.data);
        var callback      = arguments[2];
        obj.original_data = RGraph.array_clone(obj.original_data);

        function Grow_inner ()
        {
            for (var i=0; i<data.length; ++i) {
                
                if (obj.original_data[i] == null) {
                    obj.original_data[i] = [];
                }

                for (var j=0; j<data[i].length; ++j) {
                    obj.original_data[i][j] = ((totalframes - framenum)/totalframes)  * data[i][j];
                }
            }

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (framenum > 0) {
                framenum--;
                RGraph.Effects.UpdateCanvas(Grow_inner);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow_inner);
    }


    /**
    * Waterfall Grow
    * 
    * @param object obj The chart object
    * @param null Not used
    * @param function An optional function which is called when the animation is finished
    */
    RGraph.Effects.Waterfall.Grow = function (obj)
    {
        var totalFrames = 45;
        var numFrame    = 0;
        var data = RGraph.array_clone(obj.data);
        var callback = arguments[2];
        
        //Reset The data to zeros
        for (var i=0; i<obj.data.length; ++i) {
            obj.data[i] /= totalFrames;
        }
        
        /**
        * Fix the scale
        */
        if (obj.Get('chart.ymax') == null) {
            var max   = obj.getMax(data);
            var scale2 = RGraph.getScale2(obj, {'max':max});
            obj.Set('chart.ymax', scale2.max);
        }
        
        //obj.Set('chart.multiplier.x', 0);
        //obj.Set('chart.multiplier.w', 0);

        function Grow_inner ()
        {
            for (var i=0; i<obj.data.length; ++i) {
                obj.data[i] = data[i] * (numFrame/totalFrames);
            }
            
            var multiplier = Math.pow(Math.sin(((numFrame / totalFrames) * 90) / (180 / PI)), 20);
            //obj.Set('chart.multiplier.x', (numFrame / totalFrames) * multiplier);
            //obj.Set('chart.multiplier.w', (numFrame / totalFrames) * multiplier);
            
            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (numFrame++ < totalFrames) {
                RGraph.Effects.UpdateCanvas(Grow_inner);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow_inner)
    }



    /**
    * Bar chart Wave effect This effect defaults to 30 frames - which is
    * approximately half a second
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.Bar.Wave2 =
    RGraph.Effects.Bar.Wave = function (obj)
    {
        var totalframes   = (arguments[1] && arguments[1].frames) ? arguments[1].frames : 15;
        var original_data = [];

        obj.Draw();
        //var scale = RGraph.getScale2(obj, {'max':obj.max});
        obj.Set('chart.ymax', obj.scale2.max);
        RGraph.Clear(obj.canvas);
        
        for (var i=0; i<obj.data.length; ++i) {
        
            (function (idx)
            {
                original_data[i] = obj.data[i];
                obj.data[i] = typeof(obj.data[i]) == 'object' ? [] : 0;
                setTimeout(function () {Iterator(idx, totalframes);}, 100 * i)
            })(i);
        }
        
        function Iterator(idx, frames)
        {
            if (frames-- > 0) {

                // Update the data point
                if (typeof(obj.data[idx]) == 'number') {
                    obj.data[idx] = ((totalframes - frames) / totalframes) * original_data[idx]

                } else if (typeof(obj.data[idx]) == 'object') {
                    for (var k=0; k<original_data[idx].length; ++k) {
                        obj.data[idx][k] = ((totalframes - frames) / totalframes) * original_data[idx][k];
                    }
                }

                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                RGraph.Effects.UpdateCanvas(function () {Iterator(idx, frames);});
            }
        }
    }



    /**
    * HProgress Grow effect (which is also the VPogress Grow effect)
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.VProgress.Grow =
    RGraph.Effects.HProgress.Grow = function (obj)
    {
        var canvas        = obj.canvas;
        var context       = obj.context;
        var initial_value = obj.currentValue;
        var numFrames     = 30;
        var currentFrame  = 0

        if (typeof(obj.value) == 'object') {

            if (RGraph.is_null(obj.currentValue)) {
                obj.currentValue = [];
                for (var i=0; i<obj.value.length; ++i) {
                    obj.currentValue[i] = 0;
                }
            }

            var diff      = [];
            var increment = [];

            for (var i=0; i<obj.value.length; ++i) {
                diff[i]      = obj.value[i] - Number(obj.currentValue[i]);
                increment[i] = diff[i] / numFrames;
            }
            
            if (initial_value == null) {
                initial_value = [];
                for (var i=0; i< obj.value.length; ++i) {
                    initial_value[i] = 0;
                }
            }

        } else {

            var diff = obj.value - Number(obj.currentValue);
            var increment = diff  / numFrames;
        }
        var callback      = arguments[2] ? arguments[2] : null;

        function Grow ()
        {
            currentFrame++;

            if (currentFrame <= numFrames) {

                if (typeof(obj.value) == 'object') {
                    obj.value = [];
                    for (var i=0; i<initial_value.length; ++i) {
                        obj.value[i] = initial_value[i] + (increment[i] * currentFrame);
                    }
                } else {
                    obj.value = initial_value + (increment * currentFrame);
                }

                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                RGraph.Effects.UpdateCanvas(Grow);

            } else if (callback) {
                callback(obj);
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow);
    }



    /**
    * Gantt chart Grow effect
    * 
    * @param object obj The chart object
    */
    RGraph.Effects.Gantt.Grow = function (obj)
    {
        var canvas       = obj.canvas;
        var context      = obj.context;
        var numFrames    = 30;
        var currentFrame = 0;
        var callback     = arguments[2] ? arguments[2] : null;
        var events       = obj.data;
        
        var original_events = RGraph.array_clone(events);

        function Grow_gantt_inner ()
        {
            if (currentFrame <= numFrames) {
                // Update the events
                for (var i=0; i<events.length; ++i) {
                    if (typeof(events[i][0]) == 'object') {
                        for (var j=0; j<events[i].length; ++j) {
                            events[i][j][1] = (currentFrame / numFrames) * original_events[i][j][1];
                        }
                    } else {

                        events[i][1] = (currentFrame / numFrames) * original_events[i][1];
                    }
                }

                obj.data = events;

                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                currentFrame++;
                
                RGraph.Effects.UpdateCanvas(Grow_gantt_inner);

            } else if (callback) {            
                callback(obj);
            }
        }
        
        RGraph.Effects.UpdateCanvas(Grow_gantt_inner);
    }


    /**
    * This is a compatibility hack provided for Opera and Safari which
    * don't support ther Javascript 1.8.5 function.bind()
    */
    if (!Function.prototype.bind) {  
      Function.prototype.bind = function (oThis) {  
        if (typeof this !== "function") {  
          // closest thing possible to the ECMAScript 5 internal IsCallable function  
          if (console && console.log) {
            console.log('Function.prototype.bind - what is trying to be bound is not callable');
          }
        }  
      
        var aArgs = Array.prototype.slice.call(arguments, 1),   
            fToBind = this,   
            fNOP = function () {},  
            fBound = function () {  
              return fToBind.apply(this instanceof fNOP  
                                     ? this  
                                     : oThis || window,  
                                   aArgs.concat(Array.prototype.slice.call(arguments)));  
            };  
      
        fNOP.prototype = this.prototype;  
        fBound.prototype = new fNOP();  
      
        return fBound;  
      };  
    }


    /**
    * Rose chart explode
    * 
    * Explodes the Rose chart - gradually incrementing the size of the chart.explode property
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.Rose.Explode = function (obj)
    {
        var canvas   = obj.canvas;
        var opts     = arguments[1] ? arguments[1] : [];
        var callback = arguments[2] ? arguments[2] : null;
        var frames   = opts['frames'] ? opts['frames'] : 60;

        obj.Set('chart.exploded', 0);

        RGraph.Effects.Animate(obj, {'frames': frames, 'chart.exploded': Math.min(canvas.width, canvas.height)}, callback);
    }


    /**
    * Rose chart implode
    * 
    * Implodes the Rose chart - gradually decreasing the size of the chart.explode property. It starts at the largest of
    * the canvas width./height
    * 
    * @params object obj The graph object
    */
    RGraph.Effects.Rose.Implode = function (obj)
    {
        var canvas   = obj.canvas;
        var opts     = arguments[1] ? arguments[1] : [];
        var callback = arguments[2] ? arguments[2] : null;
        var frames   = opts['frames'] ? opts['frames'] : 60;

        obj.Set('chart.exploded', Math.min(canvas.width, canvas.height));

        RGraph.Effects.Animate(obj, {'frames': frames, 'chart.exploded': 0}, callback);
    }



    /**
    * Gauge Grow
    * 
    * This effect gradually increases the represented value
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Thermometer.Grow = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = 30;
        var origValue = Number(obj.currentValue);

        if (obj.currentValue == null) {
            obj.currentValue = 0
            origValue        = 0;
        }

        var newValue  = obj.value;
        var diff      = newValue - origValue;
        var step      = (diff / numFrames);
        var frame = 0;

        function Grow ()
        {
            frame++
            
            // Set the new value
            obj.value = v = ((frame / numFrames) * diff) + origValue

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (frame < 30) {
                RGraph.Effects.UpdateCanvas(Grow);
            } else if (typeof(callback) == 'function') {
                callback(obj);
            }
        }

        RGraph.Effects.UpdateCanvas(Grow);
    }


    /**
    * Trace
    * 
    * This effect is for the Scatter chart, uses the jQuery library and slowly
    * uncovers the Line/marks, but you can see the background of the chart.
    * 
    * @param object obj The graph object
    * @param object     Options - you can specify duration to set how long the effect lasts for
    */
    RGraph.Effects.Scatter.jQuery.Trace = function (obj)
    {
        var callback  = typeof(arguments[2]) == 'function' ? arguments[2] : function () {};
        var opt       = arguments[1] || [];
        
        if (!opt['duration']) {
            opt['duration'] = 1500;
        }

        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);

        /**
        * Create the DIV that the second canvas will sit in
        */
        var div = document.createElement('DIV');
            var xy = RGraph.getCanvasXY(obj.canvas);
            div.id = '__rgraph_trace_animation_' + RGraph.random(0, 4351623) + '__';
            div.style.left = xy[0] + 'px';
            div.style.top = xy[1] + 'px';
            div.style.width = obj.Get('chart.gutter.left');
            div.style.height = obj.canvas.height + 'px';
            div.style.position = 'absolute';
            div.style.overflow = 'hidden';
        document.body.appendChild(div);
        
        /**
        * Make the second canvas
        */
        var id      = '__rgraph_scatter_trace_animation_' + RGraph.random(0, 99999999) + '__';
        var canvas2 = document.createElement('CANVAS');
        canvas2.width = obj.canvas.width;
        canvas2.height = obj.canvas.height;
        canvas2.style.position = 'absolute';
        canvas2.style.left = 0;
        canvas2.style.top  = 0;
        
        // This stops the clear effect clearing the canvas - which can happen if you have multiple canvas tags on the page all with
        // dynamic effects that do redrawing
        canvas2.noclear = true;

        canvas2.id         = id;
        div.appendChild(canvas2);

        var reposition_canvas2 = function (e)
        {

            var xy = RGraph.getCanvasXY(obj.canvas);
            
            div.style.left = xy[0]   + 'px';
            div.style.top = xy[1] + 'px';
        }
        window.addEventListener('resize', reposition_canvas2, false)

        /**
        * Make a copy of the original Line object
        */
        var obj2 = new RGraph.Scatter(id, RGraph.array_clone(obj.data));

        // Remove the new line from the ObjectRegistry so that it isn't redawn
        RGraph.ObjectRegistry.Remove(obj2);

        for (i in obj.properties) {
            if (typeof(i) == 'string') {
                obj2.Set(i, obj.properties[i]);
            }
        }


        obj2.Set('chart.labels', []);
        obj2.Set('chart.background.grid', false);
        obj2.Set('chart.background.barcolor1', 'rgba(0,0,0,0)');
        obj2.Set('chart.background.barcolor2', 'rgba(0,0,0,0)');
        obj2.Set('chart.ylabels', false);
        obj2.Set('chart.noaxes', true);
        obj2.Set('chart.title', '');
        obj2.Set('chart.title.xaxis', '');
        obj2.Set('chart.title.yaxis', '');
        obj.Set('chart.key', []);
        obj2.Draw();


        /**
        * This effectively hides the line
        */
        obj.Set('chart.line.visible', false);


        RGraph.Clear(obj.canvas);
        RGraph.RedrawCanvas(obj.canvas);
        
        /**
        * Place a DIV over the canvas to stop interaction with it
        */
            if (!obj.canvas.__rgraph_scatter_trace_cover__) {
            var div2 = document.createElement('DIV');
                div2.id = '__rgraph_trace_animation_' + RGraph.random(0, 4351623) + '__';
                div2.style.left = xy[0] + 'px';
                div2.style.top = xy[1] + 'px';
                div2.style.width = obj.canvas.width + 'px';
                div2.style.height = obj.canvas.height + 'px';
                div2.style.position = 'absolute';
                div2.style.overflow = 'hidden';
                div2.style.backgroundColor = 'rgba(0,0,0,0)';
                div.div2 = div2;
                obj.canvas.__rgraph_scatter_trace_cover__ = div2
            document.body.appendChild(div2);
        } else {
            div2 = obj.canvas.__rgraph_scatter_trace_cover__;
        }

        /**
        * Animate the DIV that contains the canvas
        */
        jQuery('#' + div.id).animate({
            width: obj.canvas.width + 'px'
        }, opt['duration'], function () {

            // Remove the window resize listener
            window.removeEventListener('resize', reposition_canvas2, false);

            div.style.display  = 'none';
            div2.style.display = 'none';

            //div.removeChild(canvas2);
            obj.Set('chart.line.visible', true);

            // Revert the colors back to what they were
            obj.Set('chart.colors', RGraph.array_clone(obj2.Get('chart.colors')));
            obj.Set('chart.key', RGraph.array_clone(obj2.Get('chart.key')));

            RGraph.RedrawCanvas(obj.canvas);
            
            obj.canvas.__rgraph_trace_cover__ = null;

            callback(obj);
        });
    }



    /**
    * CornerGauge Grow
    * 
    * This effect gradually increases the represented value
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.CornerGauge.Grow = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = 30;
        var frame     = 0;

        // Single pointer
        if (typeof(obj.value) == 'number') {

            var origValue = Number(obj.currentValue);
            
            if (obj.currentValue == null) {
                obj.currentValue = obj.min;
                origValue = obj.min;
            }
    
            var newValue  = obj.value;
            var diff      = newValue - origValue;
            var step      = (diff / numFrames);
            var frame     = 0;
    

            function Grow_single ()
            {
                frame++;

                obj.value = ((frame / numFrames) * diff) + origValue

                if (obj.value > obj.max) obj.value = obj.max;
                if (obj.value < obj.min) obj.value = obj.min;
    
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
    
                if (frame < 30) {
                    RGraph.Effects.UpdateCanvas(Grow_single);
                } else if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }

            Grow_single();

        // Multiple pointers
        } else {

            if (obj.currentValue == null) {
                obj.currentValue = [];
                
                for (var i=0; i<obj.value.length; ++i) {
                    obj.currentValue[i] = obj.min;
                }
                
                origValue = RGraph.array_clone(obj.currentValue);
            }

            var origValue = RGraph.array_clone(obj.currentValue);
            var newValue  = RGraph.array_clone(obj.value);
            var diff      = [];
            var step      = [];
            
            for (var i=0; i<newValue.length; ++i) {
                diff[i] = newValue[i] - Number(obj.currentValue[i]);
                step[i] = (diff[i] / numFrames);
            }



            function Grow_multiple ()
            {
                frame++;
                
                for (var i=0; i<obj.value.length; ++i) {
                    
                    obj.value[i] = ((frame / numFrames) * diff[i]) + origValue[i];

                    if (obj.value[i] > obj.max) obj.value[i] = obj.max;
                    if (obj.value[i] < obj.min) obj.value[i] = obj.min;
        
                    RGraph.Clear(obj.canvas);
                    RGraph.RedrawCanvas(obj.canvas);
                    
                }
    
                if (frame < 30) {
                    RGraph.Effects.UpdateCanvas(Grow_multiple);
                } else if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
    
            Grow_multiple();
        }
    }



    /**
    * Rose chart Grow
    * 
    * This effect gradually increases the size of the Rose chart
    * 
    * @param object   obj The chart object
    * @param              Not used - pass null
    * @param function     An optional callback function
    */
    RGraph.Effects.Rose.Grow = function (obj)
    {
        var callback  = arguments[2];
        var numFrames = 60;
        var frame     = 0;


        function Grow ()
        {
            frame++;
            
            obj.Set('chart.animation.grow.multiplier', frame / numFrames);

            RGraph.Clear(obj.canvas);
            RGraph.RedrawCanvas(obj.canvas);

            if (frame < numFrames) {
                ++frame;
                RGraph.Effects.UpdateCanvas(Grow);
            } else {
                obj.Set('chart.animation.grow.multiplier', 1);
                RGraph.Clear(obj.canvas);
                RGraph.RedrawCanvas(obj.canvas);
                
                if (typeof(callback) == 'function') {
                    callback(obj);
                }
            }
        }

        RGraph.Effects.UpdateCanvas(Grow);
    }



    /**
    * Horizontal Scissors (open)
    * 
    * @param object   obj The graph object
    * @param @object      An array of options
    * @param function     Optional callback function
    * 
    */
    RGraph.Effects.jQuery.HScissors.Open = function (obj)
    {
        var canvas = obj.isRGraph ? obj.canvas : obj;;
        var id     = canvas.id;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var height = canvas.height / 5;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(canvas);
        RGraph.RedrawCanvas(canvas);

        for (var i=0; i<5; ++i) {
            var div = document.getElementById(id + "scissors_" + i)
            if (!div) {
                var div = document.createElement('DIV');
                    div.id = id + 'scissors_' + i;
                    div.style.width =  canvas.width + 'px';
                    div.style.height = height + 'px';
                    div.style.left   = xy[0] + 'px';
                    div.style.top   = (xy[1] + (canvas.height * (i / 5))) + 'px';
                    div.style.position = 'absolute';
                    div.style.backgroundColor = color;
                document.body.appendChild(div);
            }
    
            if (i % 2 == 0) {
                jQuery('#' + id + 'scissors_' + i).animate({left: canvas.width + 'px', width: 0}, delay);
            } else {
                jQuery('#' + id + 'scissors_' + i).animate({width: 0}, delay);
            }
        }

        setTimeout(function () {document.body.removeChild(document.getElementById(id + 'scissors_0'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + 'scissors_1'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + 'scissors_2'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + 'scissors_3'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + 'scissors_4'));}, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }



    /**
    * Horizontal Scissors (Close)
    * 
    * @param object   obj The graph object
    * @param @object      An array of options
    * @param function     Optional callback function
    * 
    */
    RGraph.Effects.jQuery.HScissors.Close = function (obj)
    {
        var canvas = obj.isRGraph ? obj.canvas : obj;
        var id     = canvas.id;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var height = canvas.height / 5;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(canvas);
        RGraph.RedrawCanvas(canvas);

        for (var i=0; i<5; ++i) {
            var div = document.createElement('DIV');
                div.id             = id + '_scissors_' + i;
                div.style.width    = 0;
                div.style.height   = height + 'px';
                div.style.left     = (i % 2 == 0 ? xy[0] + canvas.width : xy[0]) + 'px';
                div.style.top      = (xy[1] + (canvas.height * (i / 5))) + 'px';
                div.style.position = 'absolute';
                div.style.backgroundColor = color;
            document.body.appendChild(div);

            if (i % 2 == 0) {
                jQuery('#' + id + '_scissors_' + i).animate({left: xy[0] + 'px', width: canvas.width + 'px'}, delay);
            } else {
                jQuery('#' + id + '_scissors_' + i).animate({width: canvas.width + 'px'}, delay);
            }
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }



    /**
    * Vertical Scissors (open)
    * 
    * @param object   obj The graph object
    * @param @object      An array of options
    * @param function     Optional callback function
    * 
    */
    RGraph.Effects.jQuery.VScissors.Open = function (obj)
    {
        var canvas = obj.isRGraph ? obj.canvas : obj;;
        var id     = canvas.id;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var width = canvas.width / 5;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(canvas);
        RGraph.RedrawCanvas(canvas);

        for (var i=0; i<5; ++i) {
            var div = document.getElementById(id + "_vscissors_" + i)
            if (!div) {
                var div = document.createElement('DIV');
                    div.id = id + '_vscissors_' + i;
                    div.style.width =  width + 'px';
                    div.style.height = canvas.height + 'px';
                    div.style.left   = xy[0] + (canvas.width * (i / 5)) + 'px';
                    div.style.top   = xy[1] + 'px';
                    div.style.position = 'absolute';
                    div.style.backgroundColor = color;
                document.body.appendChild(div);
            }

            if (i % 2 == 0) {
                jQuery('#' + id + '_vscissors_' + i).animate({top: xy[1] + canvas.height + 'px', height: 0}, delay);
            } else {
                jQuery('#' + id + '_vscissors_' + i).animate({height: 0}, delay);
            }
        }

        setTimeout(function () {document.body.removeChild(document.getElementById(id + '_vscissors_0'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + '_vscissors_1'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + '_vscissors_2'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + '_vscissors_3'));}, delay);
        setTimeout(function () {document.body.removeChild(document.getElementById(id + '_vscissors_4'));}, delay);
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }


    /**
    * Vertical Scissors (close)
    * 
    * @param object   obj The graph object
    * @param @object      An array of options
    * @param function     Optional callback function
    * 
    */
    RGraph.Effects.jQuery.VScissors.Close = function (obj)
    {
        var canvas = obj.isRGraph ? obj.canvas : obj;
        var id     = canvas.id;
        var opts   = arguments[1] ? arguments[1] : [];
        var delay  = 1000;
        var color  = opts['color'] ? opts['color'] : 'white';
        var xy     = RGraph.getCanvasXY(canvas);
        var width  = canvas.width / 5;
        
        /**
        * First draw the chart
        */
        RGraph.Clear(canvas);
        RGraph.RedrawCanvas(canvas);

        for (var i=0; i<5; ++i) {
            var div = document.getElementById(id + "_vscissors_" + i)
            if (!div) {
                var div                = document.createElement('DIV');
                    div.id             = id + '_vscissors_' + i;
                    div.style.width    =  width + 'px';
                    div.style.height   = 0;
                    div.style.left     = xy[0] + (width * i) + 'px';
                    div.style.top      = (i % 2 == 0 ? xy[1] + canvas.height : xy[1]) + 'px';
                    div.style.position = 'absolute';
                    div.style.backgroundColor = color;
                document.body.appendChild(div);
            }

            if (i % 2 == 0) {
                jQuery('#' + id + '_vscissors_' + i).animate({top: xy[1] + 'px', height: canvas.height + 'px'}, delay);
            } else {
                jQuery('#' + id + '_vscissors_' + i).animate({height: canvas.height + 'px'}, delay);
            }
        }
        
        /**
        * Callback
        */
        if (typeof(arguments[2]) == 'function') {
            setTimeout(arguments[2], delay);
        }
    }