<html>
<head>
	<script type="text/javascript" src="js/raphael-min.js" ></script> 
	<script src="http://yui.yahooapis.com/3.18.1/build/yui/yui-min.js"></script>
	<script type="text/javascript" src="js/jsphylosvg-min.js"></script> 
	
	<script type="text/javascript">
	window.onload = function(){
		YUI().use('oop', 'json-stringify', 'io-base', 'event', 'event-delegate', function(Y){
			<? if($_GET["tax"] == "class") { ?>
			var uri = "xml/outClass.xml";
			<? }elseif($_GET["tax"] == "family") { ?>
			var uri = "xml/outFamily.xml";
			<? }elseif($_GET["tax"] == "order") { ?>
			var uri = "xml/outOrder.xml";
			<? } ?>
			function complete(id, o, args) {
				var data = o.responseXML; // Response data.
				var dataObject = {
							phyloxml: data,
							fileSource: true
						};		
				phylocanvas = new Smits.PhyloCanvas(
					dataObject,
					'svgCanvas', 
					800, 800,
					'circular'				
				);
			};
			Y.on('io:complete', complete, Y);
			var request = Y.io(uri);
		});
	};
	</script>
</head>
<body>
	<div id="svgCanvas"> </div>
</body>
</html>		