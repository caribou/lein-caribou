if (typeof window["console"] == 'undefined') {
    var console = {};
    console.log = function () {};
}

var main = (function(global){
  var init = function(){
    console.log("main inited")
  };

  return {
    init: init
  };
})(window)

/*- INITIALIZATION
----------------------------------------------------------------------*/
$(document).ready(function(){
  main.init();
});
