package com.estsoft.api.specification;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.estsoft.api.domain.Board;
import com.estsoft.util.ApiUtils;

/**
 * Board Repository 명세 정의
 * @author JSB
 */
public class BoardSpecification {
	
	/**
	 * 글 목록
	 * @param searchParams
	 * @return Specification<Board>
	 */
	public static Specification<Board> list(final Map<String, String[]> searchParams) {
		
		return new Specification<Board>() {

			@Override
			public Predicate toPredicate(Root<Board> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				if(searchParams.isEmpty()) {
					return null;
				}
				
				// 검색 조건 파라미터
				String searchType = searchParams.get("searchType")[0];
				String searchString = searchParams.get("searchString")[0];
				String searchStartDate = searchParams.get("searchStartDate")[0];
				String searchEndDate = searchParams.get("searchEndDate")[0];
				
				List<Predicate> predicates = new ArrayList<>();
				
				// delFlag = 'N'
				// 삭제되지 않은 목록만 조회
				predicates.add(cb.and(cb.equal(root.get("delFlag"), "N")));
				
				// 검색 컬럼에 따라 검색방식을 변경
				if(ApiUtils.isNotNullString(searchType) && (ApiUtils.isNotNullString(searchString) || ApiUtils.isNotNullString(searchStartDate) || ApiUtils.isNotNullString(searchEndDate))) {
					
					switch(searchType) {
					
						// 제목의 경우 LIKE 비교
						case "title" :
							predicates.add(cb.like(root.get(searchType), "%" + searchString + "%"));
							break;
							
						// 내용의 경우 LIKE 비교
						case "content" :
							predicates.add(cb.like(root.get(searchType), "%" + searchString + "%"));
							break;
							
						// 날짜 범위 비교
						case "regDate" :

							try {
								
								DateFormat df = new SimpleDateFormat("yyyy.MM.ddHH:mm:ss");

								if(ApiUtils.isNotNullString(searchStartDate)) {
									predicates.add(cb.greaterThanOrEqualTo(root.get(searchType), df.parse(searchStartDate + "00:00:00")));
								}
								
								if(ApiUtils.isNotNullString(searchEndDate)) {
									predicates.add(cb.lessThanOrEqualTo(root.get(searchType), df.parse(searchEndDate + "23:59:59")));
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							break;
						
						default :
							
							// 정수형 검색만 가능
							if(searchType.equals("no") && !ApiUtils.isStringInteger(searchString)) {
								searchString = "0";
							}
							
							predicates.add(cb.and(cb.equal(root.get(searchType), searchString)));
							break;
					}
				}
				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}

}
