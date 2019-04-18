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
	
	// 이미지 Drag & Drop
	$("#fileDrop").on("dragenter dragover", function(event) {
		
		// 기본 효과 방지
		event.preventDefault();
		
	});
	
	$("#fileDrop").on("drop", function(event) {
		
		// 기본 효과 방지
		event.preventDefault();
		
		// 드래그된 파일 정보
		var files = event.originalEvent.dataTransfer.files;
		
		// 첫번째 파일
		var file = files[0];
		
		// 콘솔에서 파일정보 확인
		console.log(file);
		
		// ajax로 전달할 폼 객체
		var formData = new FormData();
		
		// 폼 객체에 파일 추가
		formData.append("file", file);
		
		$.ajax({
			type: "POST",
			url: "/board/uploadAjax",
			data: formData,
			dataType: "text",
			processData: false,
			contentType: false,
			success: function(data) {
				
				if(data === "") {
					alert("JPG, PNG, GIF 파일만 업로드 가능합니다.");
					return;
				}
				
				var originFileName = data.substring(data.lastIndexOf("_") + 1);
				var uploadFileInput = "<input name=\"uploaded_files\" type=\"hidden\" value=\"" + data + "\">";
				
				$("#uploadedList").append(originFileName + "<br />");
				$("#uploadedList").append(uploadFileInput);
				
			}
			
		});
	});		
});

// 첨부파일 DB 저장
function saveFile(bNo) {
	
	// 첨부파일
	var uploadData = {};
	
	// 연관 글번호
	uploadData["boardNo"] = bNo;
	
	// 1. Ctrl + V 한 이미지 파일
	imgCnt = 0;
	$("#uploadFiles").find("img").each( function() {	
		imgCnt++;
		uploadData["url" + imgCnt] = $(this).attr("src");
    
	});
	
	// 2. Drag & Drop 한 이미지 파일
	$("input[name=uploaded_files]").each(function(index, item){
		imgCnt++;
		uploadData["uploaded" + imgCnt] = $(item).val();
	});
	
	
	// 첨부파일이 있는 경우
	if(Object.keys(uploadData).length > 1) {
		
		$.ajax({
	        type: "POST",
	        url: "/board/upload",
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(uploadData),
	        success:function(args){   
	        	
	        	if(args === "Y") {
	        		
	        		alert("첨부파일이 등록되었습니다.");
	        		
	        	} else if(args === "OVER") {
	        		
	        		alert("첨부파일을 5개 이상 등록하실 수 없습니다.");
	        		
	        	} else {
	        
	        		alert("첨부파일 등록에 실패했습니다.");
	        		
	        	}
	        	
	        }, 
	        error:function(e){  
	            alert("첨부파일 등록에 실패했습니다.");  
	            return;
	        } 
	
	    });
		
	}
}

