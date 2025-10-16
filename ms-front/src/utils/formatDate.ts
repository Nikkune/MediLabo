export function formatDate(input: Date | string | number): string {
    const date = input instanceof Date ? input : new Date(input);
    const now = new Date();

    const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const d = startOfDay(date);
    const n = startOfDay(now);

    const msPerDay = 24 * 60 * 60 * 1000;
    const diffDays = Math.floor((n.getTime() - d.getTime()) / msPerDay);

    if (diffDays < 0) return "in the futur ⸜(｡˃ ᵕ ˂ )⸝♡"
    if (diffDays === 0) return "Today";
    if (diffDays === 1) return "Yesterday";

    if (diffDays < 7) return `il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;

    const weeks = Math.floor(diffDays / 7);
    if (weeks === 1) return "a week ago";
    if (weeks > 1 && weeks < 5) return `${weeks} weeks ago`;

    const months = Math.floor(diffDays / 30);
    if (months === 1) return "a month ago";
    if (months > 1 && months < 12) return `${months} months ago`;

    const years = Math.floor(diffDays / 365);
    if (years === 1) return "a year ago";

    return date.toLocaleDateString("en-US", {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}