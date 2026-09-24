/**
 * Accurately formats scan timestamps in the user's local timezone (e.g. "23 Sep, 06:48 PM").
 * Converts UTC / ISO timestamps from the cloud database to the browser's local time,
 * perfectly matching mobile device time displays.
 */
export function formatScanTime(timestamp?: string, createdAt?: string): string {
  const source = createdAt || timestamp;
  if (source) {
    try {
      // If it contains a date string or ISO format
      if (source.includes('T') || (source.includes('-') && source.includes(':'))) {
        const iso = source.endsWith('Z') || source.includes('+') ? source : `${source}Z`;
        const d = new Date(iso);
        if (!isNaN(d.getTime())) {
          const day = d.toLocaleDateString('en-GB', { day: '2-digit' });
          const month = d.toLocaleDateString('en-GB', { month: 'short' });
          const time = d.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
          });
          return `${day} ${month}, ${time}`;
        }
      }
    } catch {
      // Fallback
    }
  }

  // If already formatted like "23 Sep, 06:48 PM" from mobile
  if (timestamp && timestamp !== 'Recent') {
    return timestamp;
  }

  return 'Just now';
}
