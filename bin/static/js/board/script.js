$(document).ready(function() {
	
	// 초기 게시글 목록
	if($("#boardList").length > 0 && $("#pageNum").val() === "0") {
		getBoardList(1);
	}
	
	
	// 등록
	$("#btn-save").on("click", function () {
		
		var title = $("#title").val();
		var content = $("#contents").val();
	    var delFlag = "N";
	    var modifyDate = $("#modify_date").val();
	    var regDate = $("#reg_date").val();
	    var groupNo = ($("#group_no").val()) ? $("#group_no").val() : 0;
	    var groupSeq = ($("#group_seq").val()) ? $("#group_seq").val() : 0;
	    var parentNo = ($("#parent_no").val()) ? $("#parent_no").val() : 0;
	    var depth = ($("#depth").val()) ? $("#depth").val() : 0;
				
		if(!title) {
			alert("제목을 입력하세요.");
			return false;
		}
		
		var data = {
			title: title,
		    content: content,
		    delFlag: delFlag,
		    modifyDate: modifyDate,
		    regDate: regDate,
		    groupNo: groupNo,
			groupSeq: groupSeq,
			parentNo: parentNo,
			depth: depth
		};
		
		// 답글의 경우 url 변경
		var url = (groupNo) ? "/board/saveReply" : "/board/save";
		
	    $.ajax({
	        type: "POST",
	        url: url,
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	
	        	// 첨부파일 DB 저장
	        	saveFile(result["no"]);
	        	
	        	alert("등록되었습니다.");
		        location.href = "/board/list";   
	        	
	        }, 
	        error:function(e){   
	        } 
	
	    });
		    
	});
	
	// 수정
	$("#btn-mod").on("click", function () {
		
		var bNo = $("#no").val();
		var title = $("#title").val();
		var content = $("#contents").val();
		
		if(!bNo) {
			alert("유효하지 않은 접근입니다.");
			return false;
		}
		
		if(!title) {
			alert("제목을 입력하세요.");
			$("#title").focus();
			return false;
		}
		
		var data = {
		    title: title,
		    content: content
		};
		
	    $.ajax({
	        type: "PUT",
	        url: "/board/update/" + bNo,
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	
	        	// 첨부파일 DB 저장
	        	saveFile(result["no"]);
	        	
	        	alert("수정되었습니다.")
		        location.href = "/board/detail/" + $("#no").val();   
	        	
	        }, 
	        error:function(e){  
	        } 
	
	    });
		    
	});
	
	// 삭제
	$("#btn-del").on("click", function () {
		
		var bNo = $("#no").val();
		
		if(!confirm("삭제하시겠습니까?")) {
			return false;
		}
		
	    $.ajax({
	        type: "DELETE",
	        url: "/board/delete/" + bNo,
	        success:function(args){   
	        	
	        	alert("삭제되었습니다.")
		        location.href = "/board/list";   
	        	
	        }, 
	        error:function(e){  
	        } 
	
	    });
		    
	});
	
	
	// 취소
	$("#btn-exit").on("click", function () {
		history.go(-1);
	});
	
	// 글자 수 제한
	$("#contents").on("keyup", function() {
	    if($(this).val().length > 200) {
	    	alert("글자수는 200자로 제한됩니다.");
	        $(this).val($(this).val().substring(0, 200));
	    }
	});
	
	// 첨부파일 리스트
	$("#fileList").DataTable({
		
	    "columnDefs": [{
	        "defaultContent": "",
	        "targets": "_all"
	      }],
	      
		"language": {
			"emptyTable": "데이터가 없습니다.",
			"lengthMenu": "페이지당 _MENU_ 개씩 보기",
			"info": "",
			"infoEmpty": "",
			"infoFiltered": "( _MAX_건의 데이터에서 필터링됨 )",
			"search": "검색 : ",
			"zeroRecords": "일치하는 데이터가 없습니다.",
			"loadingRecords": "로딩중...",
			"processing": " 잠시만 기다려 주세요... "
		},
		bPaginate: false,
		responsive: false,
		processing: false,
		ordering: false,
		ServerSide: false,
		searching: false,
		sAjaxSource : "/board/file/" + $("#no").val(),
        sServerMethod: "GET",
		sAjaxDataProp: "",
		columns: [
			{ data: "filename",
				
				"render": function(data, type, row){
				
			        if(type === "display"){
			        	
			        	if(data.substring(0,1) === "/") {
			        		return "<img src='/upload" + data + "'>";
			        	} else {
			        		return "<img src='/upload/" + data + "'>";
			        	}
			            
			        }
			    }
			
			},
			{ data: "filename",
				
				"render": function(data, type, row){
					
			        if(type === "display"){
			        	
			        	if(data.substring(0,1) === "/") {
			        		return "<a href='/upload" + data + "' style='text-decoration:none;'>" + data.substring(data.lastIndexOf("_") + 1) + "</a>";
			        	} else {
			        		return "<a href='/upload/" + data + "' style='text-decoration:none;'>" + data + "</a>";
			        	}   
			        }
			        
			    }
			
			},
			{ data: "regDate",
				 
				 "render": function(data, type){
					 	
					 if(data != null) {
						 date = new Date(data);
						 return date.getFullYear() + "." + zeroPad(date.getMonth()+1, 2) + "." + zeroPad(date.getDate(), 2);
					 } else {
						 return "";
					 }
				        
				 }
			},
			{ data: "no",
				 
				 "render": function(data, type){
					 	
					 var btnStr = "";
					 
					 // 작성자만 삭제 가능
					 if($("#userCheck").val() === "Y" ) {
					
						 btnStr+= "<a href='#' onclick='deleteFile(" + data + ")' class='btn btn-danger btn-icon-split'>";
						 btnStr+= "<span class='text'>삭제</span>";
						 btnStr+= "</a>";
					     
					 }
					 
					 return btnStr;
				 }
			}]
	
	});

	
	// 댓글등록
	$("#btn-reply-save").on("click", function () {
		
		var bNo = $("#no").val();
		var content = $("#contents").val();
		var groupNo = $("#group_no").val();
		var groupSeq = $("#group_seq").val();
		var parentNo = $("#parent_no").val();
		var depth = $("#depth").val();
		
		
		if(!$("#contents").val()) {
			
			alert("내용을 입력하세요.");
			$("#contents").focus();
			return false;
			
		}
		
		var data = {
			boardNo: bNo,
		    content: content,
		    delFlag: "N",
		    groupNo: (groupNo) ? groupNo : 0,
			groupSeq: (groupSeq) ? groupSeq : 0,
			parentNo: (parentNo) ? parentNo : 0,
			depth: (depth) ? depth : 0
		};
		
		
	    $.ajax({
	        type: "POST",
	        url: "/reply/save",
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	
	        	alert("등록되었습니다.");
		        location.href = "/board/detail/" + bNo;   
	        	
	        }, 
	        error:function(e){  
	        } 
	
	    });
		    
	});
	
	// 초기 댓글 목록
	if($("#replyList").length > 0 && $("#pageNum").val() === "0") {
		getReplyList(1);
	}
	
	// 댓글의 답글 등록
	$("#btn-reply-reply-save").on("click", function () {
		
		var bNo = $("#no").val();
		var rNo = $("#reply_no").val();
		var content = $("#replyModal #reply_contents").val();
		var groupNo = $("#group_no").val();
		var groupSeq = $("#group_seq").val();
		var depth = $("#depth").val();
		
		if(!$("#replyModal #reply_contents").val()) {
			
			alert("내용을 입력하세요.");
			$("#replyModal #reply_contents").focus();
			return false;
			
		}
		
		var data = {
			boardNo: bNo,
		    content: content,
		    delFlag: "N",
		    groupNo: (groupNo) ? groupNo : 0,
			groupSeq: (groupSeq) ? groupSeq : 0,
			parentNo: (rNo) ? rNo : 0,
			depth: (depth) ? depth : 0
		};
		
		
	    $.ajax({
	        type: "POST",
	        url: "/reply/saveReply",
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	
	        	alert("등록되었습니다.");
	        	location.href = "/board/detail/" + bNo;   
	        	
	        }, 
	        error:function(e){   
	        } 
	
	    });
		    
	});
	
	// 댓글수정
	$("#btn-reply-mod").on("click", function () {
		
		var bNo = $("#no").val();
		var rNo = $("#reply_no").val();
		var content = $("#replyModifyModal #reply_contents");
		
		if(!content.val()) {
			
			alert("내용을 입력하세요.");
			content.focus();
			return false;
			
		}
		
		var data = {
			boardNo: bNo,
		    content: content.val(),
		    delFlag: "N",
			parentNo: (rNo) ? rNo : 0
		};
		
	    $.ajax({
	        type: "PUT",
	        url: "/reply/update/" + rNo,
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(args){   
	        	
	        	if(args["no"] > 0) {

		        	alert("수정되었습니다.");
	        	
	        	} else {
	        		
	        		alert("작성자만 수정 가능합니다.");
	        	}
	        	
		        location.href = "/board/detail/" + bNo;   
	        	
	        }, 
	        error:function(e){   
	        } 
	
	    });
		    
	});
	
	
	
});

// 답글의 댓글 번호 설정
function setReplyNo(n) {
	$("#reply_no").val(n); 
}

// 댓글 수정 데이터 설정
function setReplyData(n) {
	
	$.ajax({
        type: "GET",
        url: "/reply/detail/" + n,
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        success:function(result){   
        	
        	// 기존 내용 설정
        	$("#replyModifyModal #reply_contents").val(result.content);
        }, 
        error:function(e){   
        } 

    });
	
}

// 댓글 삭제
function deleteReply(rNo) {
	
	if(!confirm("삭제하시겠습니까?")) {
		return false;
	}
	
	$.ajax({
        type: "DELETE",
        url: "/reply/delete/" + rNo,
        success:function(args){   
        	
        	if(args) {

            	alert("삭제되었습니다.");
            	location.href = "/board/detail/" + $("#no").val();   
        		
        	} else {

        		alert("작성자만 삭제 가능합니다.");
        		
        	}
        	
        }, 
        error:function(e){   
        } 

    });
}

// 첨부파일 삭제
function deleteFile(fNo) {
	
	if(!confirm("삭제하시겠습니까?")) {
		return false;
	}
	
	$.ajax({
        type: "DELETE",
        url: "/board/deleteFile/" + fNo,
        success:function(args){   
        	
        	if(args) {

            	alert("삭제되었습니다.");
            	location.href = "/board/detail/" + $("#no").val();
        		
        	} else {
        		
        		alert("작성자만 삭제 가능합니다.");
        		
        	}   
        	
        }, 
        error:function(e){   
        } 

    });
	
}

// 게시글 가져오기
function getBoardList(page) {
	
	$.ajax({
	    type: "GET",
	    url: "/board/list/" + page,
	    dataType: "json",
	    contentType: "application/json; charset=utf-8",
	    success:function(result){   
	    	
	    	// 페이지 정보
	    	$("#pageNum").val(page);

	    	$("#boardListPaginationInfo").text("총 " + Number(result["totalElements"]).format() + " 건");
	    	
	    	// 페이징 버튼
	    	drawPagination("board", result["totalPages"]);
	    	
	    	// 리스트
	    	drawList(result["content"]);
	    	
	    }, 
	    error:function(e){  
	    } 

	});
}

// 글 리스트
function drawList(listObj) {

	var listHTML = "";

	for(i=0; i<listObj.length; i++) {

		var list = new Array();
		list = listObj[i];
		
		// title
		var titleHTML = "";
        titleHTML+= "<a href='/board/detail/"+  list["no"] +"' style='text-decoration:none; color:#858796;'><b>";
        
        // depth 만큼 들여쓰기
        var loopNum = list["depth"];
        for(j=0; j<loopNum; j++) {
        	titleHTML+= "&nbsp&nbsp";
        }
        
        // 답글의 경우 화살표 아이콘
        if(list["depth"] > 0) {
        	titleHTML+= "<img src='/img/icon-forward.png' width=12 height=12>&nbspRE:&nbsp";
        }
        
        titleHTML+= list["title"];
        titleHTML+= "</b></a>";
        
        // regDate
        var regDate = "";
		if(list["regDate"] != null) {
			
			var date = new Date(list["regDate"]);
			regDate = date.getFullYear() + "." + zeroPad(date.getMonth()+1, 2) + "." + zeroPad(date.getDate(), 2);
			
		}	
		
		// list
        listHTML+= "<tr role='row' class='odd'>";
		listHTML+= "	<td class='dt-body-center'>" + list["no"] + "</td>";
		listHTML+= "	<td class='dt-body-left'>";
		listHTML+= 		titleHTML;
		listHTML+= "	</td>";
		listHTML+= "	<td class='dt-body-center'>" + regDate + "</td>";
		listHTML+= "	<td class='dt-body-center'>" + list["writer"] + "</td>";
		listHTML+= "</tr>";
		
	}

	$("#boardList").html(listHTML);
	
}

// 댓글 가져오기
function getReplyList(page) {
	
	$.ajax({
	    type: "GET",
	    url: "/reply/list/" + $("#no").val() + "/" + page,
	    dataType: "json",
	    contentType: "application/json; charset=utf-8",
	    success:function(result){   
	    	
	    	// 페이지 정보
	    	$("#pageNum").val(page);

	    	$("#replyListPaginationInfo").text("총 " + Number(result["totalElements"]).format() + " 건");
	    	
	    	// 페이징 버튼
	    	drawPagination("reply", result["totalPages"]);
	    	
	    	// 리스트
	    	drawReplyList(result["content"]);
	    	
	    }, 
	    error:function(e){  
	    } 

	});
}

// 댓글 리스트
function drawReplyList(listObj) {

	var listHTML = "";

	for(i=0; i<listObj.length; i++) {

		var list = new Array();
		list = listObj[i];
		
		// title
		titleHTML = "<b>";
        
        // depth 만큼 들여쓰기
        var loopNum = list["depth"];
        for(j=0; j<loopNum; j++) {
        	titleHTML+= "&nbsp&nbsp";
        }
        
        // 답글의 경우 화살표 아이콘
        if(list["depth"] > 0) {
        	titleHTML+= "<img src='/img/icon-forward.png' width=12 height=12>&nbsp&nbsp";
        }
        
        titleHTML+= list["content"];
        titleHTML+= "</b></a>";
        
        // regDate
        var regDate = "";
		if(list["regDate"] != null) {
						
			var date = new Date(list["regDate"]);
			regDate = date.getFullYear() + "." + zeroPad(date.getMonth()+1, 2) + "." + zeroPad(date.getDate(), 2) + " ";
			regDate+= zeroPad(date.getHours(), 2) + ":" + zeroPad(date.getMinutes(), 2) + ":" + zeroPad(date.getSeconds(), 2);
			
		}	
		
		// 답글, 수정, 삭제 버튼
		btnStr = "<a href='#' onclick='setReplyNo(" + list["no"] + ")' class='btn btn-success btn-icon-split' data-toggle='modal' data-target='#replyModal'>";
		btnStr+= "<span class='text'>답글</span>";
		btnStr+= "</a> ";
		btnStr+= "<a href='#' onclick='setReplyNo(" + list["no"] + "); setReplyData(" + list["no"] + ");' class='btn btn-warning btn-icon-split' data-toggle='modal' data-target='#replyModifyModal'>";
		btnStr+= "<span class='text'>수정</span>";
		btnStr+= "</a> ";
		btnStr+= "<a href='#' onclick='deleteReply(" + list["no"] + ")' class='btn btn-danger btn-icon-split'>";
		btnStr+= "<span class='text'>삭제</span>";
		btnStr+= "</a>";
		
		// list
        listHTML+= "<tr role='row' class='odd'>";
		listHTML+= "	<td class='dt-body-left'>" + titleHTML + "</td>";
		listHTML+= "	<td class='dt-body-center'>" + regDate + "</td>";
		listHTML+= "	<td class='dt-body-center'>" + list["writer"] + "</td>";
		listHTML+= "	<td class='dt-body-center'>" + btnStr + "</td>";
		listHTML+= "</tr>";
		
	}

	$("#replyList").html(listHTML);
	
}

// 페이징 버튼
function drawPagination(type, totalPages) {

	// 페이징 대상 Obj
	var targetObj = "#" + type + "ListPagination"; 
	var targetUrl = "/" + type + "/list";
	
	// 댓글은 원글 번호 필요
	if(type === "reply") {
		
		targetUrl+= "/" + $("#no").val();
		
	}
	
	var pageNum = ($("#pageNum").val() > 0) ? $("#pageNum").val() : 1;
		
	$(targetObj).bootpag({
		
		total: totalPages,
     page: pageNum,
     maxVisible: (type === "reply") ? 5 : 10,
     leaps: true,
     firstLastUse: true,
     first: "처음",
     last: "끝",
     next: "다음",
     prev: "이전",
     wrapClass: "pagination",
     activeClass: "active",
     disabledClass: "disabled",
     nextClass: "next",
     prevClass: "prev",
     lastClass: "last",
     firstClass: "first"
     	
	}).on("page", function(event, num){

		$.ajax({
		    type: "GET",
		    url: targetUrl + "/" + num,
		    dataType: "json",
		    contentType: "application/json; charset=utf-8",
		    success:function(result){   
		    	
		    	// 페이지 설정
		    	$("#pageNum").val(num);
		    	
		    	if(type === "board") {
		    		
		    		// 게시글 리스트 새로 생성
			    	drawList(result["content"]);
		    		
		    	} else if (type === "reply") {
		    		
		    		// 댓글 리스트 새로 생성
		    		drawReplyList(result["content"]);
		    	}
		    	
		    }, 
		    error:function(e){  
		    } 

		});
     
 });

	
}
