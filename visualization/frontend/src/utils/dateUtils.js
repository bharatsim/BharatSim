const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function getPeriod(hours) {
  return hours > 12 ? 'PM' : 'AM';
}

function formatDate(dateString) {
  const date = new Date(dateString);
  if (date.toString() === 'Invalid Date') return '--';
  const DD = date.getDate();
  const MM = date.getMonth();
  const YYYY = date.getFullYear();
  const HH = date.getHours();
  const mm = date.getMinutes();

  return `${DD}-${months[MM]}-${YYYY} at ${HH % 12}:${mm < 10 ? `0${mm}` : mm} ${getPeriod(HH)}`;
}

export { formatDate };
