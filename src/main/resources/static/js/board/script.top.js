$(document).ready(function() {
	
	// access_token 갱신
	$("#btn-refresh").on("click", function() {
		
	    $.ajax({
	        type: "POST",
	        url: "/member/refresh",
	        success:function(result){   
	        	
	        	if(result === "200") {
	        	
	        		// 시간 연장 성공
	        		alert("인증 시간이 연장되었습니다.");
	        		location.reload();
	        		
	        	} else if(result === "204") {
	        		
	        		// 시간 연장 실패
	        		alert("연장 가능 시간이 만료되었습니다. 재로그인 하시기 바랍니다.");
	        		location.href = "/member/logout";
	        		
	        	} else {
	        		
	        		// 시간 연장 실패
	        		alert("로그인 세션이 만료되었습니다. 재로그인 하시기 바랍니다.");
	        		location.href = "/member/logout";
	        		
	        	}
		        
	        }, 
	        error:function(e){  
	        	sendRedirect(e.status);
	        } 
	
	    });
		    
	});
	
	// 로그아웃
	$("#btn-logout").on("click", function() {
		
		if(confirm("로그아웃 하시겠습니까?")) {
			
			location.href = "/member/logout";
		}
	        
	});
	

	// 회원가입 페이지로
	$("#btn-register").on("click", function() {
		
		location.href = "/member/register";
		
	});
	
	// 로그인 페이지로
	$("#btn-login").on("click", function() {
		
		location.href = "/member/login";
		
	});
	
});


