$(document).ready(function() {
	
	// 초기 게시글 목록
	if($("#boardList").length > 0 && $("#pageNum").val() === "0") {
		getBoardList(1);
	}
	
	
	// 등록
	$("#btn-save").on("click", function() {
		
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
	$("#btn-mod").on("click", function() {
		
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
	$("#btn-del").on("click", function() {
		
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
	$("#btn-exit").on("click", function() {
		history.go(-1);
	});
	
	// 글자 수 제한
	$("#contents, #replyContents").on("keyup", function() {
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
			{ data: "fileName",
				
				"render": function(data, type, row){
				
			        if(type === "display"){
			        	
			        	return "<img src='/upload" + data + "'>";
			        	
			        }
			    }
			
			},
			{ data: "fileName",
				
				"render": function(data, type, row){
					
			        if(type === "display"){
			        	
			        	return "<a href='/board/download/" + row["no"] + "' style='text-decoration:none;'>" + data.substring(12) + "</a>";
			        	
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
	$("#btn-reply-save").on("click", function() {
		
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
	$("#btn-reply-reply-save").on("click", function() {
		
		var bNo = $("#no").val();
		var rNo = $("#reply_no").val();
		var content = $("#replyModal #replyContents").val();
		var groupNo = $("#group_no").val();
		var groupSeq = $("#group_seq").val();
		var depth = $("#depth").val();
		
		if(!$("#replyModal #replyContents").val()) {
			
			alert("내용을 입력하세요.");
			$("#replyModal #replyContents").focus();
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
	$("#btn-reply-mod").on("click", function() {
		
		var bNo = $("#no").val();
		var rNo = $("#reply_no").val();
		var content = $("#replyModifyModal #replyContents");
		
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
	
	
	// 검색 박스 변경
	$("#searchType").change(function() {
		
		var searchString = $("#searchString");
		var searchDate = $("#searchDate");
		
		if($(this).val() === "regDate") {
			
			searchString.css("display", "none");
			searchDate.css("display", "block");
			
		} else {
			
			searchString.css("display", "block");
			searchDate.css("display", "none");
			
		}
		
	});
	
	// 검색
	$("#searchString, #searchStartDate, #searchEndDate").keydown(function(key) {

		// 날짜 형식 확인
		var dateFormat = /^(\d{4}).(0[0-9]|1[0-2]).(0[1-9]|[1-2][0-9]|3[0-1])$/;
		
		// 엔터 입력 시 실행
		if(key.keyCode === 13){
		
			var searchType = $("#searchType").val();
			var searchString = $("#searchString").val().trim();
			var searchStartDate = $("#searchStartDate").val().trim();
			var searchEndDate = $("#searchEndDate").val().trim();
			
			if(!searchType) {
				location.href = "/board/list";
				return;
			}
			
			if(searchType === "regDate") {
				
				if(!searchStartDate && !searchEndDate) {
					alert("검색 날짜를 입력하세요.");
					return false;
				} 
				
				if(searchStartDate.length > 0) {

					// 날짜 형식 확인
					if(!dateFormat.test(searchStartDate)) {
						alert("검색 날짜 형식이 올바르지 않습니다.");
						return false;
					}
				}
			
				if(searchEndDate.length > 0) {
	
					// 날짜 형식 확인
					if(!dateFormat.test(searchEndDate)) {
						alert("검색 날짜 형식이 올바르지 않습니다.");
						return false;
					}
				}
				
			} else {

				if(!searchString) {
					alert("검색어를 입력하세요.");
					return false;
				}
				
			}
			
			getBoardList(1);
		}
		
	});
	
	
	// 날짜 형식 변환
	$("#searchStartDate, #searchEndDate").keydown(function(event) {
		
		// 입력 날짜
		$text = $(this);
		
		// 입력받은 문자 또는 키값
		var key = event.charCode || event.keyCode || 0;
		
		if(!checkKeyEvent(key)) {
			return false;
		}
		
		if(key !== 8 && key !== 9) {
			
			if($text.val().length === 4) {
				$text.val($text.val() + ".");
			}
			
			if($text.val().length === 7) {
			    $text.val($text.val() + ".");
			}
			
			if($text.val().length > 9) {
			    $text.val($text.val().substring(0, 9));
			}
			
		}
		
	});
	
	
	// 게시판 페이징 크기 변환
	$("#pageSize").change(function() {
		
		getBoardList(1);
		
	});
	
	// 댓글 페이징 크기 변환
	$("#replyPageSize").change(function() {
		
		getReplyList(1);
		
	});
	
	
	// 정렬 변경
	$("a[name=order]").on("click", function() {

		// 정렬 문자
		var ASC = "↑";
		var DESC = "↓";

		// 선택자
		$obj = $(this);
		
		// 정렬 설정 값
		var orderTypeObj = $("#orderType");
		var orderFieldObj = $("#orderField");
		
		// 선택 필드
		var orderField = $obj.attr("field");
		
		// 현재 정렬 상태
		var text = $obj.text();
		var orderType = text.substring(text.length - 1);
		
		// 변경 정렬 상태
		var appendOrderType = "";
		if(orderType !== undefined && orderType === DESC) {
			// 디폴트 정렬 상태
			appendOrderType = "";
		} else {
			// 정렬 상태 변경
			appendOrderType = (orderType === ASC) ? "DESC" : "ASC";
		}
		
		
		// 정렬 필드 변경 시 기존에 설정된 정렬 값 해제
		if(orderFieldObj.val().length > 0 && orderFieldObj.val() !== orderField) {
			var removeOrderObj = $("a[field=" + orderFieldObj.val() + "]");
			var removeOrderObjText = removeOrderObj.text();
			
			removeOrderObj.text(removeOrderObjText.substring(0, removeOrderObjText.length - 1));
		}
		
		// 정렬 값 설정
		orderTypeObj.val(appendOrderType);
		orderFieldObj.val(orderField);
		
		// 기존의 Direction 지우고 append
		$obj.text($obj.text().replace(ASC, ""));
		$obj.text($obj.text().replace(DESC, ""))
		$obj.append(eval(appendOrderType));
		
		getBoardList(1);
	
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
        	$("#replyModifyModal #replyContents").val(result.content);
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
	
	var url = "/board/list/" + page;
	
	// 검색 파라미터 추가
	var param = $("#form").serialize();
	
	url+= "?" + param;
	
	$.ajax({
	    type: "GET",
	    url: url,
	    success:function(result){   
	    	
	    	// 페이지 정보
	    	$("#pageNum").val(page);

	    	$("#boardListPaginationInfo").html("총 <b>" + Number(result["totalElements"]).format() + "</b> 건");
	    	
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

	// 회원정보
	var memberList = getMemberList();
	
	// 게시판 리스트 HTML
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
		
		// writer
		var writer = "";
		writer = (typeof(memberList[list["writer"]]) != "undefined") ? memberList[list["writer"]] : list["writer"];
		
		// list
        listHTML+= "<tr role='row' class='odd'>";
		listHTML+= "	<td class='dt-body-center'>" + list["no"] + "</td>";
		listHTML+= "	<td class='dt-body-left'>";
		listHTML+= 		titleHTML;
		listHTML+= "	</td>";
		listHTML+= "	<td class='dt-body-center'>" + regDate + "</td>";
		listHTML+= "	<td class='dt-body-center' title='" + list["writer"] + "'>" + writer + "</td>";
		listHTML+= "</tr>";
		
	}

	$("#boardList").html(listHTML);
	
}

// 댓글 가져오기
function getReplyList(page) {
	
	var url = "/reply/list/" + $("#no").val() + "/" + page;
	
	// 검색 파라미터 추가
	var param = $("#form").serialize();
	
	url+= "?" + param;
	
	$.ajax({
	    type: "GET",
	    url: url,
	    dataType: "json",
	    contentType: "application/json; charset=utf-8",
	    success:function(result){   
	    	
	    	// 페이지 정보
	    	$("#pageNum").val(page);

	    	$("#replyListPaginationInfo").html("총 <b>" + Number(result["totalElements"]).format() + "</b> 건");
	    	
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

	// 회원정보
	var memberList = getMemberList();
	
	// 댓글 리스트 HTML
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
		
		// writer
		var writer = "";
		writer = (typeof(memberList[list["writer"]]) != "undefined") ? memberList[list["writer"]] : list["writer"];
		
		// 답글, 수정, 삭제 버튼
		btnStr = "<a href='#' onclick='setReplyNo(" + list["no"] + ")' class='btn btn-success btn-icon-split font-size-smid' data-toggle='modal' data-target='#replyModal'>";
		btnStr+= "<span class='text'>답글</span>";
		btnStr+= "</a> ";
		btnStr+= "<a href='#' onclick='setReplyNo(" + list["no"] + "); setReplyData(" + list["no"] + ");' class='btn btn-warning btn-icon-split font-size-smid' data-toggle='modal' data-target='#replyModifyModal'>";
		btnStr+= "<span class='text'>수정</span>";
		btnStr+= "</a> ";
		btnStr+= "<a href='#' onclick='deleteReply(" + list["no"] + ")' class='btn btn-danger btn-icon-split font-size-smid'>";
		btnStr+= "<span class='text'>삭제</span>";
		btnStr+= "</a>";
		
		// list
        listHTML+= "<tr role='row' class='odd'>";
		listHTML+= "	<td class='dt-body-left'>" + titleHTML + "</td>";
		listHTML+= "	<td class='dt-body-center'>" + regDate + "</td>";
		listHTML+= "	<td class='dt-body-center' title='" + list["writer"] + "'>" + writer + "</td>";
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
	
	// 페이지
	var pageNum = ($("#pageNum").val() > 0) ? $("#pageNum").val() : 1;

	// 페이징 객체 비우고 시작
	$(targetObj).empty();
	
	// 페이징 객체 생성
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
     	
	// unbind/bind 중복 클릭 방지
	}).unbind("page").bind("page").on("page", function(event, num){

		// 검색 파라미터 추가
		var param = $("#form").serialize();
		
		$.ajax({
		    type: "GET",
		    url: targetUrl + "/" + num + "?" + param,
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


// 회원정보
function getMemberList() {
	
	var memberList = new Array();

	// 회원정보
	$.ajax({
	    type: "GET",
	    url: "/member/list",
	    dataType: "json",
	    contentType: "application/json; charset=utf-8",
	    async: false,
	    success:function(result){   
	    	
	    	// key : email, value : name
	    	for(i=0; i<result.length; i++) {
	    		memberList[result[i].email] = result[i].name;
	    	}
	    	
	    }, 
	    error:function(e){  
	    } 

	});
	
	return memberList;
	
}
