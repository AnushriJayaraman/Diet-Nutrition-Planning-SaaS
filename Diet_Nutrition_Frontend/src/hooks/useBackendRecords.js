import { useCallback, useEffect, useState } from "react";
import axios from "../api";

export default function useBackendRecords(endpoint, dataKey) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const reload = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await axios.get(endpoint);
      const rows = response.data[dataKey];
      if (!Array.isArray(rows)) {
        throw new Error(`The backend response did not contain a ${dataKey} list.`);
      }
      setRecords(rows);
    } catch (loadError) {
      setError(loadError.message || "Unable to load saved records from the backend.");
    } finally {
      setLoading(false);
    }
  }, [endpoint, dataKey]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { records, loading, error, reload };
}
