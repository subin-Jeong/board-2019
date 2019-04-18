$(document).ready(function() {
	
	// access_token 갱신
	$("#btn-refresh").on("click", function () {
		
	    $.ajax({
	        type: "POST",
	        url: "/member/refresh",
	        success:function(result){   
	        	
	        	if(result === "200") {
	        	
	        		// 시간 연장 성공
	        		alert("인증 시간이 연장되었습니다.");
	        		location.reload();
	        		
	        	} else {
	        		
	        		// 시간 연장 실패
	        		alert("인증 시간 연장에 실패하였습니다.");
	        	}
		        
	        }, 
	        error:function(e){  
	        } 
	
	    });
		    
	});
	
	// 로그아웃
	$("#btn-logout").on("click", function () {
		
		if(confirm("로그아웃 하시겠습니까?")) {
			
			location.href = "/member/logout";
		}
	        
	});
	
});


