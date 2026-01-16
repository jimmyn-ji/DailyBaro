"use strict";
const formatDateToStandard = (dateInput) => {
  if (!dateInput) return "";
  let date;
  if (typeof dateInput === "string") {
    date = new Date(dateInput);
  } else if (dateInput instanceof Date) {
    date = dateInput;
  } else if (typeof dateInput === "number") {
    date = new Date(dateInput);
  } else {
    return "";
  }
  if (isNaN(date.getTime())) {
    return "";
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};
const formatDateShort = (dateInput) => {
  const standardDate = formatDateToStandard(dateInput);
  if (!standardDate) return "";
  return standardDate.substring(5);
};
const formatDateTime = (dateInput) => {
  if (!dateInput) return "";
  let date;
  if (typeof dateInput === "string") {
    date = new Date(dateInput);
  } else if (dateInput instanceof Date) {
    date = dateInput;
  } else if (typeof dateInput === "number") {
    date = new Date(dateInput);
  } else {
    return "";
  }
  if (isNaN(date.getTime())) {
    return "";
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};
const formatDateLocal = (dateInput) => {
  if (!dateInput) return "";
  let date;
  if (typeof dateInput === "string") {
    date = new Date(dateInput);
  } else if (dateInput instanceof Date) {
    date = dateInput;
  } else if (typeof dateInput === "number") {
    date = new Date(dateInput);
  } else {
    return "";
  }
  if (isNaN(date.getTime())) {
    return "";
  }
  return date.toLocaleDateString() + " " + date.toLocaleTimeString();
};
exports.formatDateLocal = formatDateLocal;
exports.formatDateShort = formatDateShort;
exports.formatDateTime = formatDateTime;
exports.formatDateToStandard = formatDateToStandard;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/dateUtils.js.map
