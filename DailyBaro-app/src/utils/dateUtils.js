/**
 * 日期工具函数
 */

/**
 * 将各种格式的日期转换为标准日期字符串 (YYYY-MM-DD)
 * @param {any} dateInput - 日期输入，可以是字符串、Date对象、时间戳等
 * @returns {string} 标准日期字符串
 */
export const formatDateToStandard = (dateInput) => {
	if (!dateInput) return ''
	
	let date
	if (typeof dateInput === 'string') {
		// 如果是字符串，尝试解析
		date = new Date(dateInput)
	} else if (dateInput instanceof Date) {
		// 如果是Date对象，直接使用
		date = dateInput
	} else if (typeof dateInput === 'number') {
		// 如果是时间戳，转换为Date对象
		date = new Date(dateInput)
	} else {
		// 其他情况，返回空字符串
		return ''
	}
	
	// 检查日期是否有效
	if (isNaN(date.getTime())) {
		return ''
	}
	
	// 格式化为 YYYY-MM-DD
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${year}-${month}-${day}`
}

/**
 * 将各种格式的日期转换为简短日期字符串 (MM-DD)
 * @param {any} dateInput - 日期输入
 * @returns {string} 简短日期字符串
 */
export const formatDateShort = (dateInput) => {
	const standardDate = formatDateToStandard(dateInput)
	if (!standardDate) return ''
	
	// 提取月-日部分
	return standardDate.substring(5) // 去掉 YYYY- 部分
}

/**
 * 将各种格式的日期转换为完整日期时间字符串
 * @param {any} dateInput - 日期输入
 * @returns {string} 完整日期时间字符串
 */
export const formatDateTime = (dateInput) => {
	if (!dateInput) return ''
	
	let date
	if (typeof dateInput === 'string') {
		date = new Date(dateInput)
	} else if (dateInput instanceof Date) {
		date = dateInput
	} else if (typeof dateInput === 'number') {
		date = new Date(dateInput)
	} else {
		return ''
	}
	
	if (isNaN(date.getTime())) {
		return ''
	}
	
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	const hours = String(date.getHours()).padStart(2, '0')
	const minutes = String(date.getMinutes()).padStart(2, '0')
	
	return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 将各种格式的日期转换为本地化日期字符串
 * @param {any} dateInput - 日期输入
 * @returns {string} 本地化日期字符串
 */
export const formatDateLocal = (dateInput) => {
	if (!dateInput) return ''
	
	let date
	if (typeof dateInput === 'string') {
		date = new Date(dateInput)
	} else if (dateInput instanceof Date) {
		date = dateInput
	} else if (typeof dateInput === 'number') {
		date = new Date(dateInput)
	} else {
		return ''
	}
	
	if (isNaN(date.getTime())) {
		return ''
	}
	
	return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

export default {
	formatDateToStandard,
	formatDateShort,
	formatDateTime,
	formatDateLocal
}
