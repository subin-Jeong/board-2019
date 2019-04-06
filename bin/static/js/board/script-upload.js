$(document).ready(function() {
	
	// 이미지 붙여넣기
	$("#uploadFiles").on("keyup", function() {
		
		// 텍스트 내용은 삭제
		var text = $(this).contents().each( function() {
			
			if(!$(this).attr("src")) {
				$(this).remove();
			}
	    });
	});
	
	// 파일 업로드
	$(function(){
		
	      $('.demo-noninputable').pastableNonInputable();
	      $('.demo-textarea').on('focus', function(){
	        var isFocused = $(this).hasClass('pastable-focus');
	        console && console.log('[textarea] focus event fired! ' + (isFocused ? 'fake onfocus' : 'real onfocus'));
	      }).pastableTextarea().on('blur', function(){
	        var isFocused = $(this).hasClass('pastable-focus');
	        console && console.log('[textarea] blur event fired! ' + (isFocused ? 'fake onblur' : 'real onblur'));
	      });
	      $('.demo-contenteditable').pastableContenteditable();
	      $('.demo').on('pasteImage', function(ev, data){
	        var blobUrl = URL.createObjectURL(data.blob);
	        
	        // 붙여넣기한 데이터
	        var pasteImageData ="<div class=\"result\"><img src=\"" + data.dataURL +"\" ></div>";
	        
	        $('#pasteImages').append(pasteImageData);
	        
	      }).on('pasteImageError', function(ev, data){
	        alert('Oops: ' + data.message);
	        if(data.url){
	          alert('But we got its url anyway:' + data.url)
	        }
	      }).on('pasteText', function(ev, data){
	        $('<div class="result"></div>').text('text: "' + data.text + '"').insertAfter(this);
	      });
	    });
	
	
/*	// 붙여넣은 파일 확인
	$("#uploadFiles").on("keyup", function() {
		
		$("#uploadFiles").find("img").each( function() {
			
			var uploadFileURL = $(this).attr("src");
			var uploadFiles = "<input type=\"hidden\" name=\"uploadFile\" value=\"" + uploadFileURL + "\">";
			
			$(this).append(uploadFiles);
			
	    });
		
	});*/
	
	
});

