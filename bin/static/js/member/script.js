$(document).ready(function() {

	// 등록
	$("#btn-register").on("click", function () {
		
		var data = {
			email: $("#email").val(),
		    name: $("#lastName").val() + " " + $("#firstName").val(),
		    password: $("#password").val()
		};
		
	    $.ajax({
	        type: "POST",
	        url: "/member/register",
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	alert("가입이 완료되었습니다.");
	        }, 
	        error:function(e){  
	            alert(e.responseText);  
	        }
	    });
		    
	});
	
	// 로그인
	$("#btn-login").on("click", function () {
		
		var data = {
			email: $("#email").val(),
		    password: $("#password").val()
		};
		
	    $.ajax({
	        type: "POST",
	        url: "/member/login",
	        dataType: "json",
	        contentType: "application/json; charset=utf-8",
	        data: JSON.stringify(data),
	        success:function(result){   
	        	alert("로그인에 성공하였습니다.");
	        }, 
	        error:function(e){  
	            alert(e.responseText);  
	        } 
	
	    });
		    
	});
	
});



