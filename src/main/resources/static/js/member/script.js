$(document).ready(function() {

	// 회원가입
	$("#btn-register").on("click", function () {
		
		var email = $("#email");
		var lastName = $("#lastName");
		var firstName = $("#firstName");
		var password = $("#password");
		var passwordCheck = $("#passwordCheck");
		
		if(!email.val()) {
			alert("이메일을 입력하세요.");
			email.focus();
			return false;
		}
		
		if(!lastName.val()) {
			alert("이름을 입력하세요.");
			lastName.focus();
			return false;
		}
		
		if(!firstName.val()) {
			alert("성을 입력하세요.");
			firstName.focus();
			return false;
		}
		
		if(!password.val()) {
			alert("비밀번호를 입력하세요.");
			password.focus();
			return false;
		}
		
		if(!passwordCheck.val()) {
			alert("비밀번호 확인을 입력하세요.");
			passwordCheck.focus();
			return false;
		}
		
		if($("#registerCheck").val() != "Y") {
			alert("회원가입 폼을 정확히 작성해주세요.");
			return false;
		}
		
		if($("#emailUniqueCheck").val() != "Y") {
			alert("이메일 중복확인을 진행해주세요.");
			return false;
		}
		
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
	        	location.href = "/member/login";
	        }, 
	        error:function(e){  
	            alert(e.responseText);  
	        }
	    });
		    
	});
	
	// 이메일 유효성 체크
	$("#email").on("keyup", function () {
		
		var regExp = /^[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[@]{1}[-A-Za-z0-9_]+[-A-Za-z0-9_.]*[.]{1}[A-Za-z]{1,5}$/;
		
	    if (!regExp.test($(this).val())) {
	    	$("#emailCheck").html("<font color=red>유효하지 않은 이메일입니다.</font>");
	    	setRegisterCheck("N");
	    	
	    	// 이메일 중복확인 버튼 비활성화
	    	$("#btn-email-check").css("display", "none");
	    	
	    } else {
	    	$("#emailCheck").html("<font color=#4E73DF>사용 가능합니다.</font>");
	    	setRegisterCheck("Y");
	    	
	    	// 이메일 중복확인 버튼 활성화
	    	$("#btn-email-check").css("display", "block");
	    }
	    
	    // 이메일 중복확인 재진행
	    setEmailUniqueCheck("N");
	    
	});
	
	// 비밀번호 확인 유효성 체크
	$("#passwordCheck").on("keyup", function () {
		
		if ($(this).val() != $("#password").val()) {
	    	$("#pwdCheck").html("<font color=red>비밀번호가 일치하지 않습니다.</font>");
	    	setRegisterCheck("N");
	    } else {
	    	$("#pwdCheck").html("<font color=#4E73DF>비밀번호가 일치합니다.</font>");
	    	setRegisterCheck("Y");
	    }
		
	});
	
	
	// 로그인
	$("#btn-login").on("click", function () {
		
		$("#form").submit();
		return false;
		
		var data = {
			client_id: "subin",
			client_secret: "123",
			response_type: "code",
			//user_oauth_approval: "true",
			email: $("#email").val(),
		    password: $("#password").val(),
		    grant_type: "password",
		    redirect_uri: "localhost:8080/board/list"
		};
		
		alert($.param(data));
		
	    $.ajax({
	        type: "POST",
	        url: "/oauth/token",
	        dataType: "json",
	        //contentType: "application/json; charset=utf-8",
	        //data: JSON.stringify(data),
	        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
	        data: $.param(data),
	        success:function(result){   
	        	
	        	alert(result);
	        	
	        	alert("token : " + result["access_token"]);
	        	alert("로그인에 성공하였습니다.");
	        	location.href = "/board/list";
	        	
	        }, 
	        error:function(e){  
	            alert("로그인에 실패하였습니다.");  
	        } 
	
	    });
		    
	});
	
	// 이메일 중복확인
	$("#btn-email-check").on("click", function () {
		
		var data = { email: $("#email").val() };
    	
		if(data) {
		
			$.ajax({
		        type: "POST",
		        url: "/member/getMember/",
		        dataType: "json",
		        contentType: "application/json; charset=utf-8",
		        data: JSON.stringify(data),
		        success:function(result){  
		        	
		        	if(result > 0) {
		        		
		        		alert("증복된 이메일입니다.")
		        		setEmailUniqueCheck("N");
		        		
		        	} else {
		        		
		        		setEmailUniqueCheck("Y");
		        	}
		        	
		        }, 
		        error:function(e){  
		        	
		        	alert("다시 시도해주세요.")
	        		setRegisterCheck("N");
		        	
		        }
		    });
		}
    	
	});
	
});

// 회원가입 유효 여부 설정
function setRegisterCheck(flag) {
	$("#registerCheck").val(flag);
}

// 이메일 중복 확인
function setEmailUniqueCheck(flag) {
	
	$("#emailUniqueCheck").val(flag);
	
	if(flag == "Y") {
		
		$("#btn-email-check").attr("class", "btn btn-success btn-user btn-block");
		$("#btn-email-check").html("이메일 중복확인 완료");
		
	} else {
	
		$("#btn-email-check").attr("class", "btn btn-warning btn-user btn-block");
		$("#btn-email-check").html("이메일 중복확인");
	}
}
